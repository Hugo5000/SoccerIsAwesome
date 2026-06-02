package at.iamsoccer.soccerisawesome.itemrename.dialog.component;

import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.IDialogFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.component.specific.ConsumableComponentDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.component.specific.SingleIntComponentDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractButtonListDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.IActionButtonFactory;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.checkerframework.checker.index.qual.Positive;

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
public abstract class AbstractDataComponentListDialog extends AbstractButtonListDialog {
    protected final static Set<DataComponentType> EXCLUDED_COMPONENTS = Set.of(
        DataComponentTypes.ITEM_NAME,
        DataComponentTypes.CUSTOM_NAME,
        DataComponentTypes.LORE
    );

    protected final Map<DataComponentType.Valued<?>, IActionButtonFactory> dataComponentEditorDialogs = Map.ofEntries(
        new ConsumableComponentDialog(() -> this).entry(),
        new SingleIntComponentDialog(() -> this, DataComponentTypes.MAX_STACK_SIZE, item -> 1, ItemStack::getAmount, item -> 99).entry(),
        new SingleIntComponentDialog(() -> this, DataComponentTypes.MAX_DAMAGE, item -> 1, item -> 1, item -> 1_000_000).entry(), // TODO: make int input field
        new SingleIntComponentDialog(() -> this, DataComponentTypes.DAMAGE, item -> 0, item -> 0, item -> item.getData(DataComponentTypes.MAX_DAMAGE)).entry()
    );

    protected final List<Set<DataComponentType>> exclusives = List.of();
    protected final Map<DataComponentType, Function<ItemStack, Component>> requires = Map.of(
        DataComponentTypes.MAX_DAMAGE, item -> !item.hasData(DataComponentTypes.MAX_STACK_SIZE) ? null : item.getData(DataComponentTypes.MAX_STACK_SIZE) == 1 ? null : Component.text("max_stack_size must be set to 1"),
        DataComponentTypes.DAMAGE, item -> item.hasData(DataComponentTypes.MAX_DAMAGE) ? null : Component.text("Requires the max_damage Component"),
        DataComponentTypes.MAX_STACK_SIZE, item -> !item.hasData(DataComponentTypes.MAX_DAMAGE) ? null : Component.text("You're not allowed to change this while you have the max_damage Component")
    );
    protected final Map<DataComponentType, ? extends IActionButtonFactory> basicDataComponentEditorDialogs =
        RegistryAccess.registryAccess().getRegistry(RegistryKey.DATA_COMPONENT_TYPE).stream()
            .collect(Collectors.toUnmodifiableMap(
                dataComponentType -> dataComponentType,
                dataComponentType -> new BasicDataComponentEditorDialog(permission, () -> this, dataComponentType))
            );

    private final DialogAction action = DialogAction.customClick((response, audience) -> {
        if (!(audience instanceof Player player)) return;
        audience.showDialog(this.create(player));
    }, UNLIMITED_CALLBACK_OPTIONS);

    public AbstractDataComponentListDialog(Permission permission, Supplier<IDialogFactory> returnFactorySupplier) {
        super(permission, returnFactorySupplier);
    }

    private List<DataComponentType> getAllUnsetDataType(ItemStack itemStack) {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.DATA_COMPONENT_TYPE).stream()
            .filter(type -> type.isPersistent())
            .filter(type -> !type.key().value().contains("/"))
            .filter(Predicate.not(EXCLUDED_COMPONENTS::contains))
            .filter(type -> filter(type, itemStack)).toList();
    }

    protected abstract boolean filter(DataComponentType type, ItemStack itemStack);

    @Override
    protected List<ActionButton> getDialogButtons(Player player, ItemStack item) {
        return getAllUnsetDataType(item)
            .stream()
            .map(type -> {/*TODO: replace with actual click*/
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
                    var res = function.apply(item);
                    if (res != null)
                        return ActionButton.builder(Component.text(type.key().asMinimalString(), NamedTextColor.RED))
                            .tooltip(Component.text("This Component requires the following: ").append(res))
                            .action(action).build();
                }
                if (dataComponentEditorDialogs.containsKey(type)) {
                    return dataComponentEditorDialogs.get(type).openActionButton();
                }
                if (item.hasData(type) || !item.hasData(type) && item.getType().asItemType().hasDefaultData(type)) {
                    return basicDataComponentEditorDialogs.get(type).openActionButton();
                }
                return ActionButton.builder(Component.text(type.key().asMinimalString(), NamedTextColor.RED)).action(action).build();
            })
            .toList();
    }

    @Override
    protected @Positive int getColumns() {
        return 2;
    }
}
