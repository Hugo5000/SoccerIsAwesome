package at.iamsoccer.soccerisawesome.woodcutter;

import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.AbstractModule;
import at.iamsoccer.soccerisawesome.SoccerIsAwesomePlugin;
import co.aikar.commands.PaperCommandManager;
import net.kyori.adventure.key.Key;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.StonecuttingRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WoodCutter extends AbstractModule {
    private YamlFileConfig config;
    private boolean verbose = false;

    private final Set<NamespacedKey> registeredRecipes = new HashSet<>();

    public WoodCutter(SoccerIsAwesomePlugin plugin) {
        super(plugin, "WoodCutter");
    }

    @Override
    public boolean enable(PaperCommandManager commandManager) {
        if (!super.enable(commandManager)) return false; // should never fail
        config = new YamlFileConfig(plugin, "woodcutter-config.yml");
        return true;
    }

    @Override
    public boolean disable(PaperCommandManager commandManager) {
        if (!super.disable(commandManager)) return false; // should never fail
        tryRemoveStonecutterRecipes();
        Bukkit.getServer().updateRecipes();
        return true;
    }

    @Override
    public void reload() {
        config.reload();
        verbose = config.getBoolean("verbose", false);
        tryRemoveStonecutterRecipes();
        tryCreateStonecutterRecipes();
        Bukkit.getServer().updateRecipes();
    }

    private void tryCreateStonecutterRecipes() {
        parseItemTypesFromConfigAt("recipes", "from", "to", (inputItemType, resultItemTypes, amount) -> {
            if (verbose)
                plugin.info("[WoodCutter] Adding recipes: %s -> %s * %s".formatted(inputItemType.key().asMinimalString(), amount, resultItemTypes.stream().map(i -> i.key().asMinimalString()).collect(Collectors.joining(", "))));
            for (ItemType resultItemType : resultItemTypes) {
                registerWoodCutterRecipe(inputItemType, resultItemType, amount);
            }
        });
        parseItemTypesFromConfigAt("reverse-recipes", "to", "from", (resultItemType, inputItemTypes, amount) -> {
            if (verbose)
                plugin.info("[WoodCutter] Adding recipes: %s -> %s * %s".formatted(inputItemTypes.stream().map(i -> i.key().asMinimalString()).collect(Collectors.joining(", ")), amount, resultItemType.key().asMinimalString()));
            for (ItemType inputItemType : inputItemTypes) {
                registerWoodCutterRecipe(inputItemType, resultItemType, amount);
            }
        });
        if (!verbose) plugin.info("[WoodCutter] Registered %s recipes!".formatted(registeredRecipes.size()));
    }

    private void parseItemTypesFromConfigAt(String configLocation, String primaryKey, String secondaryKey, TriConsumer<ItemType, List<ItemType>, Integer> resultConsumer) {
        for (Map<?, ?> map : config.getMapList(configLocation)) {
            if (!(map.get(primaryKey) instanceof String primaryPattern) || !(map.get(secondaryKey) instanceof String secondaryPattern)) {
                plugin.warn("[WoodCutter] Could not parse " + map);
                continue;
            }
            final int amount = map.get("amount") instanceof Integer integer ? integer : 1;
            @Nullable final String track = map.get("track") instanceof String s ? s : null;
            var items = getItemsMatching(primaryPattern)
                .collect(Collectors.toMap(MatchResult::itemType, matchResult ->
                    getItemsMatching(replacePlaceHolders(secondaryPattern, matchResult))
                        .map(MatchResult::itemType)
                        .toList()
                ));
            if (items.isEmpty()) {
                plugin.warn("[WoodCutter] No matches for item pattern \"%s\"!".formatted(primaryPattern));
                continue;
            }
            items.forEach((itemType, resultItemTypes) -> {
                if (resultItemTypes.isEmpty()) {
                    plugin.warn("[WoodCutter] No secondary matches for item %s with pattern \"%s\": \"%s\"!".formatted(itemType.key().asMinimalString(), primaryPattern, secondaryPattern.replace("$key", itemType.key().namespace()).replace("$value", itemType.key().value())));
                    return;
                }
                resultConsumer.accept(itemType, resultItemTypes, amount);
            });
        }
    }

    private String replacePlaceHolders(String pattern, MatchResult matchResult) {
        StringBuilder sb = new StringBuilder(pattern.length() + 64);
        int len = pattern.length();

        for (int i = 0; i < len; i++) {
            final char c = pattern.charAt(i);
            if (c == '$' && ++i < len) {
                sb.append(switch (pattern.charAt(i)) {
                    case '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' ->
                        matchResult.matcher == null ? "" : matchResult.matcher.group(Integer.parseInt(String.valueOf(pattern.charAt(i))));
                    case 'k' -> matchResult.itemType.key().namespace();
                    case 'v' -> matchResult.itemType.key().value();
                    default -> '$' + pattern.charAt(i);
                });
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private Stream<MatchResult> getItemsMatching(String pattern) {
        if (Key.parseable(pattern)) {
            @Nullable var item = Registry.ITEM.get(Key.key(pattern));
            return item == null ? Stream.empty() : Stream.of(new MatchResult(item, null));
        }
        pattern = "^(?:minecraft:)?(?:" + pattern + ")$";
        var pat = Pattern.compile(pattern);
        return Registry.ITEM
            .stream()
            .mapMulti((itemType, consumer) -> {
                var matcher = pat.matcher(itemType.key().asString());
                if (matcher.matches()) consumer.accept(new MatchResult(itemType, matcher));
            });
    }

    private record MatchResult(ItemType itemType, @Nullable Matcher matcher) {
    }

    private void registerWoodCutterRecipe(ItemType fromItem, ItemType toItem, int amount) {
        try {
            final NamespacedKey key = NamespacedKey.fromString(fromItem.key().value() + "_to_" + toItem.key().value(), plugin);
            final StonecuttingRecipe recipe = new StonecuttingRecipe(
                Objects.requireNonNull(key),
                toItem.createItemStack(amount),
                Objects.requireNonNull(fromItem.asMaterial())
            );
            Bukkit.getServer().addRecipe(recipe, false);
            registeredRecipes.add(key);
        } catch (Exception e) {
            plugin.severe("Failed to create WoodCutter recipe %s -> %s".formatted(fromItem.key().asMinimalString(), toItem.key().asMinimalString()), e);
        }
    }

    private void tryRemoveStonecutterRecipes() {
        // this should never fail, since it only unregisters registered recipes
        for (var key : registeredRecipes) {
            Bukkit.getServer().removeRecipe(key, false);
        }
        registeredRecipes.clear();
    }
}
