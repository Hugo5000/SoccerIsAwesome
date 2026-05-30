package at.iamsoccer.soccerisawesome.itemrename.dialog.component;

import at.iamsoccer.soccerisawesome.itemrename.dialog.IDialogFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.IExternalDialogFactory;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static at.iamsoccer.soccerisawesome.itemrename.dialog.rename.AbstractRenameDialog.UNLIMITED_CALLBACK_OPTIONS;

public class BasicDataComponentEditorDialog implements IExternalDialogFactory {
    public final Permission permission;
    public final Supplier<IDialogFactory> returnDialogFactorySupplier;

    private final DataComponentType dataComponentType;

    public BasicDataComponentEditorDialog(
        Permission permission, Supplier<IDialogFactory> returnDialogFactorySupplier,
        DataComponentType dataComponentType
    ) {
        this.permission = permission;
        this.returnDialogFactorySupplier = returnDialogFactorySupplier;
        this.dataComponentType = dataComponentType;
    }

    public final DialogAction openAction = DialogAction.customClick(this::onOpen, UNLIMITED_CALLBACK_OPTIONS);

    private void onOpen(DialogResponseView dialogResponseView, Audience audience) {
        if (!(audience instanceof Player player) || !player.hasPermission(permission)) return;
        player.showDialog(create(player, true));
    }

    private final DialogAction resetComponentAction = DialogAction.customClick(this::onResetComponent, UNLIMITED_CALLBACK_OPTIONS);
    private final DialogAction removeComponentAction = DialogAction.customClick(this::onRemoveComponent, UNLIMITED_CALLBACK_OPTIONS);
    private final DialogAction cancelAction = DialogAction.customClick(this::onCancel, UNLIMITED_CALLBACK_OPTIONS);


    @Override
    @NotNull
    public DialogLike create(Player player, boolean returnToMain) {
        var item = player.getInventory().getItemInMainHand().clone();
        var actions = new ArrayList<ActionButton>(2);
        if (item.hasData(dataComponentType) && !item.getType().asItemType().hasDefaultData(dataComponentType)
            || !item.hasData(dataComponentType) && item.getType().asItemType().hasDefaultData(dataComponentType)
            || dataComponentType instanceof DataComponentType.Valued<?> valued && !item.getData(valued).equals(item.getType().asItemType().getDefaultData(valued))) {
            actions.add(ActionButton.builder(Component.text("Reset Component to Default")).action(resetComponentAction).build());
        }
        if (item.hasData(dataComponentType)) {
            actions.add(ActionButton.builder(Component.text("Remove Component")).action(removeComponentAction).build());
        }
        return Dialog.create(builderFactory -> {
                var builder = builderFactory.empty().base(DialogBase.builder(Component.text(dataComponentType.key().asMinimalString()))
                    .body(List.of(DialogBody.item(item).build()))
                    .inputs(List.of())
                    .canCloseWithEscape(true)
                    .afterAction(DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE)
                    .pause(false)
                    .build());
                builder.type(DialogType.multiAction(actions, ActionButton.builder(Component.text("Cancel")).action(cancelAction).build(), 1));
            }
        );
    }

    @Override
    public boolean hasPermission(Player player) {
        return player.hasPermission(permission);
    }

    private void onResetComponent(DialogResponseView response, Audience audience) {
        if (!(audience instanceof Player player) || !player.hasPermission(permission)) return;
        player.getInventory().getItemInMainHand().resetData(dataComponentType);
        onCancel(response, audience);
    }

    private void onRemoveComponent(DialogResponseView response, Audience audience) {
        if (!(audience instanceof Player player) || !player.hasPermission(permission)) return;
        var item = player.getInventory().getItemInMainHand();
        if (item.getType().asItemType().hasDefaultData(dataComponentType)) item.unsetData(dataComponentType);
        else item.resetData(dataComponentType);
        onCancel(response, audience);
    }

    private void onCancel(DialogResponseView response, Audience audience) {
        if (!(audience instanceof Player player) || !player.hasPermission(permission) || !returnDialogFactorySupplier.get().hasPermission(player))
            return;
        player.showDialog(returnDialogFactorySupplier.get().create(player, true));
    }

    @Override
    public ActionButton actionButton() {
        return ActionButton.builder(Component.text(dataComponentType.key().asMinimalString(), NamedTextColor.YELLOW))
            .tooltip(Component.text("This has not been fully implemented, you can only remove or reset it to default."))
            .action(openAction)
            .build();
    }

    @Override
    public DialogAction dialogAction() {
        return openAction;
    }
}
