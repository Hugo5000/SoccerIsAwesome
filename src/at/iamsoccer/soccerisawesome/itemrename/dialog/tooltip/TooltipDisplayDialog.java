package at.iamsoccer.soccerisawesome.itemrename.dialog.tooltip;

import at.iamsoccer.soccerisawesome.itemrename.dialog.IDialogFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.special.AbstractButtonListDialog;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static at.iamsoccer.soccerisawesome.itemrename.dialog.rename.AbstractRenameDialog.UNLIMITED_CALLBACK_OPTIONS;

public class TooltipDisplayDialog extends AbstractButtonListDialog {
    public TooltipDisplayDialog(Permission permission, Supplier<IDialogFactory> returnDialogFactorySupplier) {
        super(permission, returnDialogFactorySupplier);
    }

    private final DialogAction hideAllAction = DialogAction.customClick(this::onHideAll, UNLIMITED_CALLBACK_OPTIONS);
    private final DialogAction showAllAction = DialogAction.customClick(this::onShowAll, UNLIMITED_CALLBACK_OPTIONS);
    private final DialogAction hideTooltipAction = DialogAction.customClick(this::onHideTooltip, UNLIMITED_CALLBACK_OPTIONS);

    private final SpecificTooltipDisplayDialog specificTooltipDisplayDialog = new SpecificTooltipDisplayDialog(permission, () -> this);

    @Override
    protected List<ActionButton> getDialogButtons(Player player, ItemStack item) {
        return List.of(
            ActionButton.builder(Component.text("Hide All")).action(hideAllAction).build(),
            ActionButton.builder(Component.text("Show All")).action(showAllAction).build(),
            ActionButton.builder(Component.text("Hide Tooltip")).action(hideTooltipAction).build(),
            specificTooltipDisplayDialog.actionButton()
        );
    }

    private void onHideAll(DialogResponseView response, Audience audience) {
        if (!(audience instanceof Player player) || !player.hasPermission(permission)) return;
        var item = player.getInventory().getItemInMainHand();
        var tooltip = TooltipDisplay.tooltipDisplay().hiddenComponents(getComponents(item)).build();
        item.setData(DataComponentTypes.TOOLTIP_DISPLAY, tooltip);
        player.showDialog(create(player, true));
    }

    private void onShowAll(DialogResponseView response, Audience audience) {
        if (!(audience instanceof Player player) || !player.hasPermission(permission)) return;
        var item = player.getInventory().getItemInMainHand();
        var tooltip = TooltipDisplay.tooltipDisplay().build();
        item.setData(DataComponentTypes.TOOLTIP_DISPLAY, tooltip);
        player.showDialog(create(player, true));
    }

    private void onHideTooltip(DialogResponseView response, Audience audience) {
        if (!(audience instanceof Player player) || !player.hasPermission(permission)) return;
        var item = player.getInventory().getItemInMainHand();
        var tooltip = TooltipDisplay.tooltipDisplay().hideTooltip(true).build();
        item.setData(DataComponentTypes.TOOLTIP_DISPLAY, tooltip);
        player.showDialog(create(player, true));
    }

    private Set<DataComponentType> getComponents(ItemStack itemStack) {
        var set = new HashSet<DataComponentType>();
        set.addAll(itemStack.getDataTypes());
        set.addAll(itemStack.getType().asItemType().getDefaultDataTypes());
        return set;
    }
}
