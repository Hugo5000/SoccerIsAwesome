package at.iamsoccer.soccerisawesome.itemrename.dialog.tooltip;

import at.iamsoccer.soccerisawesome.itemrename.dialog.IDialogFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.IExternalDialogFactory;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static at.iamsoccer.soccerisawesome.itemrename.dialog.rename.AbstractRenameDialog.UNLIMITED_CALLBACK_OPTIONS;

public class SpecificTooltipDisplayDialog implements IExternalDialogFactory {
    private final Supplier<IDialogFactory> returnDialogFactorySupplier;
    private final Permission permission;

    public SpecificTooltipDisplayDialog(Permission permission, Supplier<IDialogFactory> returnDialogFactorySupplier) {
        this.returnDialogFactorySupplier = returnDialogFactorySupplier;
        this.permission = permission;
    }

    private final DialogAction openAction = DialogAction.customClick(this::onOpen, UNLIMITED_CALLBACK_OPTIONS);
    private final DialogAction applyAction = DialogAction.customClick(this::onApply, UNLIMITED_CALLBACK_OPTIONS);
    private final DialogAction cancelAction = DialogAction.customClick(this::onCancel, UNLIMITED_CALLBACK_OPTIONS);

    @Override
    public DialogLike create(Player player, boolean returnToMain) {
        var item = player.getInventory().getItemInMainHand();
        var inputs = new ArrayList<DialogInput>();
        var current = item.getDataOrDefault(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().build());
        getComponents(item).stream()
            .map(dataType -> DialogInput.bool(getCompKey(dataType), Component.text("Hide " + dataType.key().asMinimalString()))
                .initial(current.hiddenComponents().contains(dataType))
                .build()
            ).forEach(inputs::add);
        return Dialog.create(builder ->
            builder.empty().base(DialogBase.builder(Component.text("Edit Tooltip Components"))
                    .body(List.of())
                    .inputs(inputs)
                    .build())
                .type(DialogType.confirmation(
                    ActionButton.builder(Component.text("Apply")).action(applyAction).build(),
                    ActionButton.builder(Component.text("Cancel")).action(cancelAction).build()
                ))
        );
    }

    private Set<DataComponentType> getComponents(ItemStack itemStack) {
        var set = new HashSet<DataComponentType>();
        set.addAll(itemStack.getDataTypes());
        set.addAll(itemStack.getType().asItemType().getDefaultDataTypes());
        return set;
    }

    private void onOpen(DialogResponseView response, Audience audience) {
        if (!(audience instanceof Player player) || !player.hasPermission(permission)) return;
        player.showDialog(create(player, true));
    }

    private void onApply(DialogResponseView response, Audience audience) {
        if (!(audience instanceof Player player) || !player.hasPermission(permission)) return;
        var hiddenComponents = new HashSet<DataComponentType>();
        var item = player.getInventory().getItemInMainHand();
        for (var comp : getComponents(item)) {
            if (response.getBoolean(getCompKey(comp))) {
                hiddenComponents.add(comp);
            }
        }
        var tooltip = TooltipDisplay.tooltipDisplay().hiddenComponents(hiddenComponents).build();
        item.setData(DataComponentTypes.TOOLTIP_DISPLAY, tooltip);
        onCancel(response, audience);
    }

    private static @NonNull String getCompKey(DataComponentType comp) {
        return comp.key().asString().replace(":", "_");
    }

    private void onCancel(DialogResponseView response, Audience audience) {
        if (!(audience instanceof Player player) || !player.hasPermission(permission) || !returnDialogFactorySupplier.get().hasPermission(player))
            return;
        player.showDialog(returnDialogFactorySupplier.get().create(player, true));
    }

    @Override public ActionButton actionButton() {
        return ActionButton.builder(Component.text("Edit Tooltip")).action(openAction).build();
    }

    @Override public DialogAction dialogAction() {
        return openAction;
    }

    @Override
    public boolean hasPermission(Player player) {
        return player.hasPermission(permission);
    }
}
