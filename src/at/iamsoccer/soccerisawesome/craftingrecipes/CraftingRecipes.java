package at.iamsoccer.soccerisawesome.craftingrecipes;

import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.AbstractModule;
import at.iamsoccer.soccerisawesome.SoccerIsAwesomePlugin;
import co.aikar.commands.PaperCommandManager;
import net.kyori.adventure.key.Key;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CraftingRecipes extends AbstractModule implements Listener {
    private YamlFileConfig config;
    private boolean verbose = false;

    private final Set<NamespacedKey> registeredRecipes = new HashSet<>();
    private final List<Recipe> removedRecipes = new ArrayList<>();

    public CraftingRecipes(SoccerIsAwesomePlugin plugin) {
        super(plugin, "CraftingRecipes");
    }

    @Override
    public boolean enable(PaperCommandManager commandManager) {
        if (!super.enable(commandManager)) return false; // should never fail
        config = new YamlFileConfig(plugin, "crafting-config.yml");
        return true;
    }

    @Override
    public boolean disable(PaperCommandManager commandManager) {
        if (!super.disable(commandManager)) return false; // should never fail
        if (Bukkit.isStopping()) return true;
        resetRecipes();
        Bukkit.getServer().updateRecipes();
        return true;
    }

    @Override
    public void reload() {
        config.reload();
        verbose = config.getBoolean("verbose", false);
        resetRecipes();
        tryCreateRecipes();
        Bukkit.getServer().updateRecipes();
        Bukkit.getOnlinePlayers().forEach(player -> player.discoverRecipes(registeredRecipes));
    }

    private void tryCreateRecipes() {
        parseRecipesFromConfigAt("shapeless", (resultStack, ingredientTypes, map) -> {
            if (map.get("replace") instanceof Boolean bool && bool) {
                removeRecipesFor(resultStack);
            }
            CraftingBookCategory category = getCategory(map);
            if (category == null) return;
            if (verbose)
                info("Adding shapeless recipe: %s <- %s".formatted(resultStack.getType().key().asMinimalString(), ingredientTypes.stream().map(itemType -> itemType.key().asMinimalString()).collect(Collectors.joining(", "))));
            registerShapelessRecipe(resultStack, ingredientTypes, category);
        });
        parseRecipesFromConfigAt("shaped", (resultStack, ingredientTypes, map) -> {
            if (!(map.get("shape") instanceof List<?> list) || list.stream().anyMatch(Predicate.not(String.class::isInstance))) {
                warn("Missing shape for shaped recipe: %s".formatted(map));
                return;
            }
            CraftingBookCategory category = getCategory(map);
            if (category == null) return;

            if (map.get("replace") instanceof Boolean bool && bool) {
                removeRecipesFor(resultStack);
            }
            if (verbose)
                info("Adding shaped recipe: %s <- %s".formatted(resultStack.getType().key().asMinimalString(), ingredientTypes.stream().map(itemType -> itemType.key().asMinimalString()).collect(Collectors.joining(", "))));
            //noinspection unchecked, its checked right above
            registerShapedRecipe(resultStack, ingredientTypes, (List<String>) list, category);
        });
        if (!verbose)

            info("Registered %s recipes!".formatted(registeredRecipes.size()));
    }

    private @Nullable CraftingBookCategory getCategory(Map<?, ?> map) {
        if (!(map.get("category") instanceof String categoryString)) {
            warn("Missing category (one of %s) for shaped recipe: %s".formatted(Arrays.stream(CraftingBookCategory.values()).map(Enum::name).collect(Collectors.joining(", ")), map));
            return null;
        }
        CraftingBookCategory category;
        try {
            category = CraftingBookCategory.valueOf(categoryString.toUpperCase());
        } catch (IllegalArgumentException e) {
            warn("Unknown category \"%s\", should be one of %s".formatted(categoryString, Arrays.stream(CraftingBookCategory.values()).map(Enum::name).collect(Collectors.joining(", "))));
            return null;
        }
        return category;
    }

    private void removeRecipesFor(ItemStack resultStack) {
        List<Recipe> recipes = Bukkit.getRecipesFor(resultStack);
        removedRecipes.addAll(recipes);
        for (Recipe recipe : recipes) {
            NamespacedKey key;
            if (recipe instanceof ShapelessRecipe shapeless) {
                key = shapeless.getKey();
            } else if (recipe instanceof ShapedRecipe shaped) {
                key = shaped.getKey();
            } else {
                continue;
            }
            Bukkit.removeRecipe(key);
        }
    }

    private void parseRecipesFromConfigAt(String configLocation, TriConsumer<ItemStack, List<ItemType>, Map<?, ?>> consumer) {
        for (Map<?, ?> map : config.getMapList(configLocation)) {

            var results = parseItems(map.get("result"), null);
            if (results == null) {
                warn("No result pattern found in: %s".formatted(map.get("result")));
                continue;
            }
            if (results.results.isEmpty()) {
                warn("Result pattern \"%s\" matched no items!".formatted(results.pattern));
                continue;
            }
            var ingredientsList = map.get("ingredients") instanceof List<?> list ? list : null;
            if (ingredientsList == null || ingredientsList.stream().anyMatch(Predicate.not(String.class::isInstance))) {
                warn("Ingredients are not a list: %s".formatted(map.get("ingredients")));
                continue;
            }
            if (ingredientsList.size() > 9) {
                warn("Too many ingredients specified, maximum of 9: %s".formatted(map.get("ingredients")));
                continue;
            }
            var ingredients = results.results.stream()
                .map(imr -> ingredientsList.stream()
                    .map(String.class::cast)
                    .map(str -> replacePlaceHolders(str, imr))
                    .map(pattern -> new MatchResults(pattern, getItemsMatching(pattern).map(MatchResult::itemType).toList()))
                    .toList()
                ).toList();

            if (ingredients.stream().anyMatch(list -> list.stream().anyMatch(Objects::isNull))) {
                warn("An ingredient for \"%s\"has the wrong format!".formatted(results.pattern));
                continue;
            }
            if (ingredients.stream().anyMatch(list -> list.stream().anyMatch(mr -> mr.results.isEmpty()))) {
                ingredients.forEach(list -> list.stream().filter(mr -> mr.results.isEmpty()).forEach(mr -> {
                    warn("Could not find any match for ingredient \"%s\"!".formatted(mr.pattern));
                }));
            }
            for (int i = 0; i < results.results.size(); i++) {
                var resultItem = results.results.get(i).itemStack;
                var ingreds = ingredients.get(i);
                if (ingreds.isEmpty() || ingreds.stream().anyMatch(mr -> mr.results.isEmpty())) continue;
                var ingredientItems = new ArrayList<ItemType>(ingreds.size());
                Stack<Integer> indexStack = new Stack<>();
                ingreds.forEach(t -> indexStack.add(0));
                do {
                    // do it for the currently saved variation
                    for (int imrIndex = 0; imrIndex < ingreds.size(); imrIndex++) {
                        ingredientItems.add(ingreds.get(imrIndex).results.get(indexStack.get(imrIndex)));
                    }
                    consumer.accept(resultItem, ingredientItems, map);
                    ingredientItems.clear();
                    // advance index
                    while (!indexStack.isEmpty()) {
                        var index = indexStack.pop() + 1;
                        var itemIndex = indexStack.size();
                        if (index < ingreds.get(itemIndex).results.size()) {
                            indexStack.add(index);
                            while (indexStack.size() < ingreds.size()) indexStack.add(0);
                            break;
                        }
                    }
                } while (!indexStack.isEmpty());
            }
        }
    }

    private @Nullable ItemMatchResults parseItems(@Nullable Object mapValue, @Nullable ItemMatchResult itemMatchResult) {
        if (mapValue == null) return null;
        if (mapValue instanceof String pattern) {
            pattern = replacePlaceHolders(pattern, itemMatchResult);
            return new ItemMatchResults(pattern, getItemsMatching(pattern)
                .map(matchResult -> new ItemMatchResult(matchResult.itemType.createItemStack(), matchResult.matcher))
                .toList()
            );
        }
        if (!(mapValue instanceof Map<?, ?> map)) return null;
        int amount = map.get("amount") instanceof Integer integer ? integer : 1;
        String pattern = map.get("material") instanceof String string ? string : null;
        if (pattern == null) return null;
        pattern = replacePlaceHolders(pattern, itemMatchResult);
        return new ItemMatchResults(pattern, getItemsMatching(pattern)
            .map(matchResult -> new ItemMatchResult(matchResult.itemType.createItemStack(amount), matchResult.matcher))
            .toList()
        );
    }

    private String replacePlaceHolders(String pattern, @Nullable ItemMatchResult itemMatchResult) {
        if (itemMatchResult == null) return pattern;
        StringBuilder sb = new StringBuilder(pattern.length() + 64);
        int len = pattern.length();

        for (int i = 0; i < len; i++) {
            final char c = pattern.charAt(i);
            if (c == '$' && ++i < len) {
                sb.append(switch (pattern.charAt(i)) {
                    case '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' ->
                        itemMatchResult.matcher == null ? "" : itemMatchResult.matcher.group(Integer.parseInt(String.valueOf(pattern.charAt(i))));
                    case 'k' -> itemMatchResult.itemStack.getType().key().namespace();
                    case 'v' -> itemMatchResult.itemStack.getType().key().value();
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

    private record ItemMatchResults(String pattern, List<ItemMatchResult> results) {
    }

    private record MatchResults(String pattern, List<ItemType> results) {
    }

    private record MatchResult(ItemType itemType, @Nullable Matcher matcher) {
    }

    private record ItemMatchResult(ItemStack itemStack, @Nullable Matcher matcher) {
    }

    private void registerShapedRecipe(ItemStack result, List<ItemType> inputs, List<String> list, CraftingBookCategory category) {
        try {
            final NamespacedKey key = NamespacedKey.fromString(result.getType().key().value() + "_from_" + inputs.stream().map(i -> i.key().value()).collect(Collectors.joining("_")), plugin);
            final ShapedRecipe recipe = new ShapedRecipe(key, result);
            recipe.shape(list.toArray(String[]::new));
            for (int i = 0; i < inputs.size(); i++) {
                recipe.setIngredient(String.valueOf(i + 1).charAt(0), inputs.get(i).asMaterial());
            }
            recipe.setCategory(category);
            Bukkit.addRecipe(recipe, false);
            registeredRecipes.add(key);
        } catch (Exception e) {
            severe("Failed to create recipe %s -> %s".formatted(inputs.stream().map(i -> i.key().asMinimalString()).collect(Collectors.joining(", ")), result.getType().key().asMinimalString()), e);
        }
    }

    private void registerShapelessRecipe(ItemStack result, List<ItemType> inputs, CraftingBookCategory category) {
        try {
            final NamespacedKey key = NamespacedKey.fromString(result.getType().key().value() + "_from_" + inputs.stream().map(i -> i.key().value()).collect(Collectors.joining("_")), plugin);
            final ShapelessRecipe recipe = new ShapelessRecipe(key, result);
            inputs.stream().map(ItemType::asMaterial).forEach(recipe::addIngredient);
            recipe.setCategory(category);
            Bukkit.addRecipe(recipe, false);
            registeredRecipes.add(key);
        } catch (Exception e) {
            severe("Failed to create recipe %s -> %s".formatted(inputs.stream().map(i -> i.key().asMinimalString()).collect(Collectors.joining(", ")), result.getType().key().asMinimalString()), e);
        }
    }

    private void resetRecipes() {
        // this should never fail, since it only unregisters registered recipes
        for (var key : registeredRecipes) {
            Bukkit.removeRecipe(key, false);
        }
        registeredRecipes.clear();
        for (var recipe : removedRecipes) {
            Bukkit.addRecipe(recipe, false);
        }
        removedRecipes.clear();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().discoverRecipes(registeredRecipes);
    }
}
