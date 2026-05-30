package at.iamsoccer.soccerisawesome.itemrename.dialog.special;

import at.iamsoccer.soccerisawesome.itemrename.ItemRenameModule;
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
import org.bukkit.permissions.Permission;

import java.util.ArrayList;
import java.util.List;

import static at.iamsoccer.soccerisawesome.itemrename.dialog.rename.AbstractRenameDialog.UNLIMITED_CALLBACK_OPTIONS;

public class SpecialFlagsDialog {
    private final ItemRenameModule renameModule;
    private final Permission permission;

    public SpecialFlagsDialog(ItemRenameModule renameModule, Permission permission) {
        this.renameModule = renameModule;
        this.permission = permission;
    }

    private DialogAction applyAction = DialogAction.customClick(this::onApply, UNLIMITED_CALLBACK_OPTIONS);
    private DialogAction cancelAction = DialogAction.customClick(this::onCancel, UNLIMITED_CALLBACK_OPTIONS);

    public DialogLike create(Player player) {
        var item = player.getInventory().getItemInMainHand();
        var inputs = new ArrayList<DialogInput>();
        item.getDataTypes()
            .forEach(dataType -> {
                // TODO: add boolean input for hiding TooltipDisplay
            });
        return Dialog.create(builder ->
            builder.empty().base(DialogBase.builder(Component.text("Dialog Title"))
                    .body(List.of())
                    .inputs(inputs)
                    .build())
                .type(DialogType.confirmation(
                    ActionButton.builder(Component.text("Apply")).build(),
                    ActionButton.builder(Component.text("Cancel")).action(applyAction).build()
                ))
        );
    }

    private void onApply(DialogResponseView response, Audience audience) {
        if (!(audience instanceof Player player) || !player.hasPermission(permission)) return;

    }

    private void onCancel(DialogResponseView dialogResponseView, Audience audience) {
        if (!(audience instanceof Player player) || !player.hasPermission(permission) || renameModule.mainRenameDialog.hasPermission(player))
            return;
        player.showDialog(renameModule.mainRenameDialog.create(player, true));
    }

}
