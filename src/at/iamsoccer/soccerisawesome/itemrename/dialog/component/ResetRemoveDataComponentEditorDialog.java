package at.iamsoccer.soccerisawesome.itemrename.dialog.component;

import at.iamsoccer.soccerisawesome.itemrename.ItemRenameModule;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractButtonListDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.IDialogFactory;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static at.iamsoccer.soccerisawesome.itemrename.dialog.rename.AbstractRenameDialog.UNLIMITED_CALLBACK_OPTIONS;

@SuppressWarnings("UnstableApiUsage")
public class ResetRemoveDataComponentEditorDialog extends AbstractButtonListDialog {
    private final DataComponentType dataComponentType;

    public ResetRemoveDataComponentEditorDialog(
        @Nullable Supplier<IDialogFactory> returnDialogFactorySupplier,
        DataComponentType dataComponentType
    ) {
        super(ItemRenameModule.createPermission(dataComponentType), returnDialogFactorySupplier);
        this.dataComponentType = dataComponentType;
    }

    private final DialogAction resetComponentAction = DialogAction.customClick(this::onResetComponent, UNLIMITED_CALLBACK_OPTIONS);
    private final DialogAction removeComponentAction = DialogAction.customClick(this::onRemoveComponent, UNLIMITED_CALLBACK_OPTIONS);

    @Override
    protected List<ActionButton> getDialogButtons(Player player, ItemStack item) {
        var actions = new ArrayList<ActionButton>(2);
        if (item.hasData(dataComponentType) && !item.getType().asItemType().hasDefaultData(dataComponentType)
            || !item.hasData(dataComponentType) && item.getType().asItemType().hasDefaultData(dataComponentType)
            || dataComponentType instanceof DataComponentType.Valued<?> valued && !item.getData(valued).equals(item.getType().asItemType().getDefaultData(valued))) {
            actions.add(ActionButton.builder(Component.text("Reset Component to Default")).action(resetComponentAction).build());
        }
        if (item.hasData(dataComponentType)) {
            actions.add(ActionButton.builder(Component.text("Remove Component")).action(removeComponentAction).build());
        }
        return actions;
    }

    private void onResetComponent(DialogResponseView response, Audience audience) {
        if (!(audience instanceof Player player) || !hasPermission(player)) return;
        player.getInventory().getItemInMainHand().resetData(dataComponentType);
        returnToPrevious(audience);
    }

    private void onRemoveComponent(DialogResponseView response, Audience audience) {
        if (!(audience instanceof Player player) || !hasPermission(player)) return;
        var item = player.getInventory().getItemInMainHand();
        if (item.getType().asItemType().hasDefaultData(dataComponentType)) item.unsetData(dataComponentType);
        else item.resetData(dataComponentType);
        returnToPrevious(audience);
    }

    @Override
    protected DialogButton.ButtonInfo openButtonInfo(Player player) {
        return new DialogButton.ButtonInfo(
            titleProvider(),
            Component.text("This has not been fully implemented, you can only remove or reset it to default.")
        );
    }

    @Override
    protected Component titleProvider() {
        return Component.text(dataComponentType.key().asMinimalString(), NamedTextColor.YELLOW);
    }
}
