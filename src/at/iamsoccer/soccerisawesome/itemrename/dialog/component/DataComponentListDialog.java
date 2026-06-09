package at.iamsoccer.soccerisawesome.itemrename.dialog.component;

import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.itemrename.dialog.component.primitives.BooleanComponentDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.component.primitives.NonValuedComponentEditorDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.component.primitives.SingleIntComponentDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.component.specific.ConsumableComponentDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.component.specific.EquipableComponentDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.component.specific.FoodComponentDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractButtonListDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractDialogButtonFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractDialogFactory;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static at.iamsoccer.soccerisawesome.itemrename.dialog.rename.AbstractRenameDialog.UNLIMITED_CALLBACK_OPTIONS;

@SuppressWarnings("UnstableApiUsage")
public class DataComponentListDialog extends AbstractButtonListDialog {
    private final IDataComponentDialogFilter filter;
    private final int columns;

    protected final static Set<DataComponentType> EXCLUDED_COMPONENTS = Set.of(
        DataComponentTypes.ITEM_NAME,
        DataComponentTypes.CUSTOM_NAME,
        DataComponentTypes.LORE,
        DataComponentTypes.TOOLTIP_DISPLAY
    );

    protected final Map<DataComponentType.Valued<?>, AbstractDialogButtonFactory<Player>> dataComponentEditorDialogs = Map.ofEntries(
        new ConsumableComponentDialog(() -> this).entry(),
        new EquipableComponentDialog(() -> this).entry(),
        new FoodComponentDialog(() -> this).entry(),
        new SingleIntComponentDialog(() -> this, DataComponentTypes.MAX_STACK_SIZE, item -> 1, ItemStack::getAmount, item -> 99).entry(),
        new SingleIntComponentDialog(() -> this, DataComponentTypes.MAX_DAMAGE, item -> 1, item -> 1, item -> 1_000_000).entry(), // TODO: make int input field
        new SingleIntComponentDialog(() -> this, DataComponentTypes.DAMAGE, item -> 0, item -> 0, item -> item.getData(DataComponentTypes.MAX_DAMAGE)).entry(),
        new BooleanComponentDialog(() -> this, DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, item -> item.hasData(DataComponentTypes.ENCHANTMENTS) && !item.getData(DataComponentTypes.ENCHANTMENTS).enchantments().isEmpty()).entry()
    );

    protected final List<Set<DataComponentType>> exclusives = List.of();
    protected final Map<DataComponentType, Function<ItemStack, @Nullable Component>> requires = Map.of(
        DataComponentTypes.MAX_DAMAGE, item -> !item.hasData(DataComponentTypes.MAX_STACK_SIZE) ? null : item.getData(DataComponentTypes.MAX_STACK_SIZE) == 1 ? null : Component.text("max_stack_size must be set to 1"),
        DataComponentTypes.DAMAGE, item -> item.hasData(DataComponentTypes.MAX_DAMAGE) ? null : Component.text("Requires the max_damage Component"),
        DataComponentTypes.MAX_STACK_SIZE, item -> !item.hasData(DataComponentTypes.MAX_DAMAGE) ? null : Component.text("You're not allowed to change this while you have the max_damage Component")
    );

    protected final Map<DataComponentType, AbstractDialogButtonFactory<Player>> basicDataComponentEditorDialogs =
        RegistryAccess.registryAccess().getRegistry(RegistryKey.DATA_COMPONENT_TYPE).stream()
            .collect(Collectors.toUnmodifiableMap(
                dataComponentType -> dataComponentType,
                dataComponentType -> {
                    if (dataComponentType instanceof DataComponentType.NonValued nonValued)
                        return new NonValuedComponentEditorDialog(() -> this, nonValued);
                    if (dataComponentType instanceof DataComponentType.Valued<?> valued) {
                        // TODO: make generic translations
                    }
                    return new ResetRemoveDataComponentEditorDialog(() -> this, dataComponentType);
                }
            ));

    private final DialogAction action = DialogAction.customClick((response, audience) -> {
        if (!(audience instanceof Player player)) return;
        open(player);
    }, UNLIMITED_CALLBACK_OPTIONS);

    @FunctionalInterface
    public interface IDataComponentDialogFilter {
        boolean test(DataComponentType type, ItemStack item, AbstractDialogButtonFactory<Player> buttonFactory);
    }

    public DataComponentListDialog(@Nullable Permission permission, @Nullable Supplier<AbstractDialogFactory<Player>> returnFactorySupplier, IDataComponentDialogFilter filter) {
        this(permission, returnFactorySupplier, filter, 2);
    }

    public DataComponentListDialog(@Nullable Permission permission, @Nullable Supplier<AbstractDialogFactory<Player>> returnFactorySupplier, IDataComponentDialogFilter filter, int columns) {
        super(permission, returnFactorySupplier);
        this.filter = filter;
        this.columns = columns;
    }

    private List<DataComponentType> getAllUnsetDataType(ItemStack itemStack) {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.DATA_COMPONENT_TYPE).stream()
            .filter(type -> type.isPersistent())
            .filter(type -> !type.key().value().contains("/"))
            .filter(Predicate.not(EXCLUDED_COMPONENTS::contains))
            .filter(type -> filter.test(type, itemStack,
                dataComponentEditorDialogs.containsKey(type)
                    ? dataComponentEditorDialogs.get(type)
                    : basicDataComponentEditorDialogs.get(type)
            )).toList();
    }

    @Override
    protected List<ActionButton> getDialogButtons(Player player, ItemStack item) {
        return getAllUnsetDataType(item)
            .stream()
            .map(type -> {
                Set<DataComponentType> foundConflicts = new HashSet<>();
                for (Set<DataComponentType> set : exclusives) {
                    if (!set.contains(type)) continue;
                    for (DataComponentType other : set) {
                        if (other.equals(type)) continue;
                        if (item.hasData(other)) foundConflicts.add(other);
                    }
                }
                if (!foundConflicts.isEmpty()) {
                    return ActionButton.builder(Component.text(type.key().asMinimalString(), NamedTextColor.RED))
                        .tooltip(Component.text("This Component is conflicting with: " + foundConflicts.stream().map(d -> d.key().asMinimalString()).collect(Collectors.joining(", "))))
                        .action(action).build();
                }
                if (requires.containsKey(type)) {
                    var function = requires.get(type);
                    @Nullable var res = function.apply(item);
                    if (res != null)
                        return ActionButton.builder(Component.text(type.key().asMinimalString(), NamedTextColor.RED))
                            .tooltip(Component.text("This Component requires the following: ").append(res))
                            .action(action).build();
                }
                if (dataComponentEditorDialogs.containsKey(type) && dataComponentEditorDialogs.get(type).isAllowedToOpen(player)) {
                    if (columns == 1) {
                        return dataComponentEditorDialogs.get(type).openActionButton(player, 200);
                    } else {
                        return dataComponentEditorDialogs.get(type).openActionButton(player);
                    }
                }
                if(basicDataComponentEditorDialogs.get(type).isAllowedToOpen(player)) {
                    if (!(basicDataComponentEditorDialogs.get(type) instanceof ResetRemoveDataComponentEditorDialog) || item.hasData(type) || !item.hasData(type) && item.getType().asItemType().hasDefaultData(type)) {
                        if (columns == 1) {
                            return basicDataComponentEditorDialogs.get(type).openActionButton(player, 200);
                        } else {
                            return basicDataComponentEditorDialogs.get(type).openActionButton(player);
                        }
                    }
                } else {
                    return ActionButton.builder(Component.text(type.key().asMinimalString(), NamedTextColor.RED))
                        .tooltip(Component.text("You are missing permissions to edit this Component.", NamedTextColor.RED))
                        .action(action).build();
                }
                return ActionButton.builder(Component.text(type.key().asMinimalString(), NamedTextColor.RED))
                    .tooltip(Component.text("This has not been implemented yet.", NamedTextColor.YELLOW))
                    .action(action).build();
            })
            .toList();
    }

    @Override
    protected @Positive int getColumns() {
        return columns;
    }

    @Override
    public void reload(YamlFileConfig configFile, ConfigurationSection configSection) {
        super.reload(configFile, configSection);
        dataComponentEditorDialogs.values().forEach(basic -> basic.reload(configFile, configSection));
        basicDataComponentEditorDialogs.values().forEach(basic -> basic.reload(configFile, configSection));
    }
}
