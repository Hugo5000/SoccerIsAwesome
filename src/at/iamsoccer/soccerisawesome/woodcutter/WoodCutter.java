package at.iamsoccer.soccerisawesome.woodcutter;

import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.AbstractModule;
import at.iamsoccer.soccerisawesome.SoccerIsAwesomePlugin;
import co.aikar.commands.PaperCommandManager;
import com.google.common.base.Functions;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.StonecuttingRecipe;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        final var registry = Registry.ITEM;
        parseItemTypesFromConfigAt(registry, "recipes", "from", "to", (inputItemType, resultItemTypes, amount) -> {
            if (verbose)
                plugin.info("[WoodCutter] Adding recipes: %s -> %s * %s".formatted(inputItemType.key().asMinimalString(), amount, resultItemTypes.stream().map(i -> i.key().asMinimalString()).collect(Collectors.joining(", "))));
            for (ItemType resultItemType : resultItemTypes) {
                registerWoodCutterRecipe(inputItemType, resultItemType, amount);
            }
        });
        parseItemTypesFromConfigAt(registry, "reverse-recipes", "to", "from", (resultItemType, inputItemTypes, amount) -> {
            if (verbose)
                plugin.info("[WoodCutter] Adding recipes: %s -> %s * %s".formatted(inputItemTypes.stream().map(i -> i.key().asMinimalString()).collect(Collectors.joining(", ")), amount, resultItemType.key().asMinimalString()));
            for (ItemType inputItemType : inputItemTypes) {
                registerWoodCutterRecipe(inputItemType, resultItemType, amount);
            }
        });
        if (!verbose) plugin.info("[WoodCutter] Registered %s recipes!".formatted(registeredRecipes.size()));
    }

    private void parseItemTypesFromConfigAt(Registry<ItemType> registry, String configLocation, String primaryKey, String secondaryKey, TriConsumer<ItemType, List<ItemType>, Integer> consumer) {
        for (Map<?, ?> map : config.getMapList(configLocation)) {
            if (!(map.get(primaryKey) instanceof String inputPattern) || !(map.get(secondaryKey) instanceof String resultPattern)) {
                plugin.warn("[WoodCutter] Could not parse " + map);
                continue;
            }
            int amount = map.get("amount") instanceof Integer integer ? integer : 1;
            var pattern = Pattern.compile("^(?:minecraft:)?" + inputPattern + "$");
            var items = registry.stream()
                .filter(item -> pattern.matcher(item.key().asMinimalString()).matches())
                .collect(Collectors.toMap(Functions.identity(), item -> {
                        var match = pattern.matcher(item.key().asMinimalString());
                        match.matches();
                        var resPatternString = "^(?:minecraft:)?" + resultPattern
                            .replace("$value", item.key().value())
                            .replace("$key", item.key().namespace()) + "$";
                        for (int i = 0; i <= match.groupCount(); i++) {
                            resPatternString = resPatternString.replace("$" + i, match.group(i));
                        }
                        var resPattern = Pattern.compile(resPatternString);
                        return registry.stream()
                            .filter(item1 -> resPattern.matcher(item1.key().asString()).matches())
                            .toList();
                    }
                ));
            if (items.isEmpty()) {
                plugin.warn("[WoodCutter] No matches for item pattern \"%s\"!".formatted(inputPattern));
                continue;
            }
            items.forEach((itemType, resultItemTypes) -> {
                if (resultItemTypes.isEmpty()) {
                    plugin.warn("[WoodCutter] No secondary matches for item %s with pattern \"%s\": \"%s\"!".formatted(itemType.key().asMinimalString(), inputPattern, resultPattern.replace("$key", itemType.key().namespace()).replace("$value", itemType.key().value())));
                    return;
                }
                consumer.accept(itemType, resultItemTypes, amount);
            });
        }
    }

    private void registerWoodCutterRecipe(ItemType fromItem, ItemType toItem, int amount) {
        try {
            final NamespacedKey key = NamespacedKey.fromString(fromItem.key().value() + "_to_" + toItem.key().value(), plugin);
            final StonecuttingRecipe recipe = new StonecuttingRecipe(
                Objects.requireNonNull(key),
                toItem.createItemStack(amount),
                Objects.requireNonNull(fromItem.asMaterial())
            );
            Bukkit.getServer().addRecipe(recipe);
            registeredRecipes.add(key);
        } catch (Exception e) {
            plugin.severe("Failed to create WoodCutter recipe %s -> %s".formatted(fromItem.key().asMinimalString(), toItem.key().asMinimalString()), e);
        }
    }

    private void tryRemoveStonecutterRecipes() {
        // this should never fail, since it only unregisters registered recipes
        for (var key : registeredRecipes) {
            Bukkit.getServer().removeRecipe(key);
        }
        registeredRecipes.clear();
    }
}
