package at.iamsoccer.soccerisawesome.itemrename.dialog.component;

import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.itemrename.IConfigSectionReloadable;
import at.iamsoccer.soccerisawesome.itemrename.dialog.IDialogFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.IExternalDialogFactory;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

import static at.iamsoccer.soccerisawesome.itemrename.dialog.rename.AbstractRenameDialog.UNLIMITED_CALLBACK_OPTIONS;

public class DataComponentEditorDialog<DataComponent> implements IExternalDialogFactory {
    @FunctionalInterface
    public interface IDialogResponseParser<DataComponent> {
        @Nullable DataComponent parseResponse(@NonNull DialogResponseView response, @Nullable DataComponent currentComponent);
    }

    @FunctionalInterface
    public interface IDialogInputProvider<DataComponent> {
        @NonNull List<? extends DialogInput> parseResponse(@Nullable DialogResponseView response, @NotNull ItemStack itemStack, @Nullable DataComponent currentComponent);
    }

    public final Permission permission;
    public final Supplier<IDialogFactory> returnDialogFactorySupplier;

    private final DataComponentType.Valued<DataComponent> dataComponentType;
    private final IDialogInputProvider<DataComponent> inputs;
    private final IDialogResponseParser<DataComponent> parseResponse;

    public DataComponentEditorDialog(
        Permission permission, Supplier<IDialogFactory> returnDialogFactorySupplier,
        DataComponentType.Valued<DataComponent> dataComponentType,
        IDialogInputProvider<DataComponent> inputs,
        IDialogResponseParser<DataComponent> parseResponse
    ) {
        this.permission = permission;
        this.returnDialogFactorySupplier = returnDialogFactorySupplier;
        this.dataComponentType = dataComponentType;
        this.inputs = inputs;
        this.parseResponse = parseResponse;
    }

    public final DialogAction openAction = DialogAction.customClick(this::onOpen, UNLIMITED_CALLBACK_OPTIONS);

    private void onOpen(DialogResponseView dialogResponseView, Audience audience) {
        if (!(audience instanceof Player player) || !player.hasPermission(permission)) return;
        player.showDialog(create(player, true));
    }

    private final DialogAction applyAction = DialogAction.customClick(this::onApply, UNLIMITED_CALLBACK_OPTIONS);
    private final DialogAction previewAction = DialogAction.customClick(this::onPreview, UNLIMITED_CALLBACK_OPTIONS);

    private final DialogAction applyActionAndReturn = DialogAction.customClick(this::onApplyAndReturn, UNLIMITED_CALLBACK_OPTIONS);
    private final DialogAction previewActionAndReturn = DialogAction.customClick(this::onPreviewAndReturn, UNLIMITED_CALLBACK_OPTIONS);
    private final DialogAction cancelAndReturn = DialogAction.customClick(this::onCancelAndReturn, UNLIMITED_CALLBACK_OPTIONS);


    @Override
    @NotNull
    public DialogLike create(Player player, boolean returnToMain) {
        return create(player, returnToMain, null);
    }

    @NotNull
    private DialogLike create(Player player, boolean returnToMain, DialogResponseView response) {
        var item = player.getInventory().getItemInMainHand().clone();
        if (response != null)
            item.setData(dataComponentType, parseResponse.parseResponse(response, item.getData(dataComponentType)));
        return Dialog.create(builderFactory -> {
                var builder = builderFactory.empty().base(DialogBase.builder(Component.text(dataComponentType.key().asMinimalString()))
                    .body(List.of(DialogBody.item(item).build()))
                    .inputs(inputs.parseResponse(response, item, item.getData(dataComponentType)))
                    .canCloseWithEscape(true)
                    .afterAction(DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE)
                    .pause(false)
                    .build());
                if (returnToMain) {
                    builder.type(DialogType.multiAction(List.of(
                        ActionButton.builder(Component.text("Preview")).action(previewActionAndReturn).build(),
                        ActionButton.builder(Component.text("Apply")).action(applyActionAndReturn).build()
                    ), ActionButton.builder(Component.text("Cancel")).action(cancelAndReturn).build(), 1));
                } else {
                    builder.type(DialogType.multiAction(List.of(
                        ActionButton.builder(Component.text("Preview")).action(previewAction).build(),
                        ActionButton.builder(Component.text("Apply")).action(applyAction).build()
                    ), ActionButton.builder(Component.text("Cancel")).build(), 1));
                }
            }
        );
    }

    @Override
    public boolean hasPermission(Player player) {
        return player.hasPermission(permission);
    }

    private void onApply(DialogResponseView response, Audience audience) {
        if (!(audience instanceof Player player) || !player.hasPermission(permission)) return;
        var item = player.getInventory().getItemInMainHand();
        var dataComponent = parseResponse.parseResponse(response, item.getData(dataComponentType));
        if (dataComponent == null) {
            // TODO: some verification/message to the user in a dialog
        } else {
            applyToItem(item, dataComponent);
        }
    }

    private void onApplyAndReturn(DialogResponseView response, Audience audience) {
        onApply(response, audience);
        onCancelAndReturn(response, audience);
    }

    private void applyToItem(ItemStack item, DataComponent data) {
        item.setData(dataComponentType, data);
    }

    private void onPreview(DialogResponseView response, Audience audience) {
        if (!(audience instanceof Player player) || !player.hasPermission(permission)) return;
        audience.showDialog(create(player, false, response));
    }

    private void onPreviewAndReturn(DialogResponseView response, Audience audience) {
        if (!(audience instanceof Player player) || !player.hasPermission(permission)) return;
        audience.showDialog(create(player, true, response));
    }

    private void onCancelAndReturn(DialogResponseView response, Audience audience) {
        if (!(audience instanceof Player player) || !player.hasPermission(permission) || !returnDialogFactorySupplier.get().hasPermission(player))
            return;
        player.showDialog(returnDialogFactorySupplier.get().create(player, true));
    }

    @Override
    public ActionButton actionButton() {
        return ActionButton.builder(Component.text(dataComponentType.key().asMinimalString())).action(openAction).build();
    }

    @Override
    public DialogAction dialogAction() {
        return openAction;
    }
}
