package at.iamsoccer.soccerisawesome.itemrename.dialog.component;

import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractBasicDialogFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractExternalDialogFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.IDialogFactory;
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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import static at.iamsoccer.soccerisawesome.itemrename.dialog.rename.AbstractRenameDialog.UNLIMITED_CALLBACK_OPTIONS;

@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractDataComponentEditorDialog<DataComponent> extends AbstractBasicDialogFactory {
    protected final DataComponentType.Valued<DataComponent> dataComponentType;

    public AbstractDataComponentEditorDialog(
        Supplier<IDialogFactory> returnDialogFactorySupplier,
        DataComponentType.Valued<DataComponent> dataComponentType
    ) {
        super(createPermission(dataComponentType), returnDialogFactorySupplier);
        this.dataComponentType = dataComponentType;
    }

    private static <DataComponent> Permission createPermission(DataComponentType.Valued<DataComponent> dataComponentType) {
        final String permissionName = "shia.rename.component." + dataComponentType.key().asString().replace(":", ".");
        @Nullable var perm = Bukkit.getPluginManager().getPermission(permissionName);
        if (perm != null) return perm;
        perm = new Permission(permissionName, "Allows you to add a %s component".formatted(dataComponentType.key().asMinimalString()), PermissionDefault.OP);
        Bukkit.getServer().getPluginManager().addPermission(perm);
        return perm;
    }

    private final DialogAction applyActionAndReturn = DialogAction.customClick(this::onApplyAndReturn, UNLIMITED_CALLBACK_OPTIONS);
    private final DialogAction previewActionAndReturn = DialogAction.customClick(this::onPreviewAndReturn, UNLIMITED_CALLBACK_OPTIONS);

    public abstract List<? extends DialogInput> parseResponseToInputs(@Nullable DialogResponseView response, ItemStack itemStack, @Nullable DataComponent currentComponent);
    public abstract @Nullable DataComponent parseResponseToComponent(DialogResponseView response, ItemStack itemStack, @Nullable DataComponent currentComponent);

    @Override
    public DialogLike create(Player player) {
        return create(player, null);
    }

    public Map.Entry<DataComponentType.Valued<DataComponent>, AbstractDataComponentEditorDialog<DataComponent>> entry() {
        return Map.entry(dataComponentType, this);
    }

    private DialogLike create(Player player, @Nullable DialogResponseView response) {
        var item = player.getInventory().getItemInMainHand().clone();
        if (response != null)
            item.setData(dataComponentType, parseResponseToComponent(response, item, item.getData(dataComponentType)));
        return createDialog(Component.text(dataComponentType.key().asMinimalString()),
            body -> {
                body.add(DialogBody.item(item).build());
                return body;
            }, parseResponseToInputs(response, item, item.getData(dataComponentType)), closeButton -> DialogType.multiAction(List.of(
                ActionButton.builder(Component.text("Preview")).action(previewActionAndReturn).build(),
                ActionButton.builder(Component.text("Apply")).action(applyActionAndReturn).build()
            ), closeButton.button(), 1)
        );
    }

    private void onApply(DialogResponseView response, Audience audience) {
        if (!(audience instanceof Player player) || !hasPermission(player)) return;
        var item = player.getInventory().getItemInMainHand();
        @Nullable
        var dataComponent = parseResponseToComponent(response, item, item.getData(dataComponentType));
        if (dataComponent == null) {
            // TODO: some verification/message to the user in a dialog
        } else {
            applyToItem(item, dataComponent);
        }
    }

    private void onApplyAndReturn(DialogResponseView response, Audience audience) {
        onApply(response, audience);
        returnToPrevious(audience);
    }

    private void applyToItem(ItemStack item, DataComponent data) {
        item.setData(dataComponentType, data);
    }

    private void onPreviewAndReturn(DialogResponseView response, Audience audience) {
        if (!(audience instanceof Player player) || !hasPermission(player)) return;
        audience.showDialog(create(player, response));
    }

    protected static float getValue(@Nullable DialogResponseView responseView, String key, float defaultValue) {
        if (responseView == null) return defaultValue;
        return Objects.requireNonNullElse(responseView.getFloat(key), defaultValue);
    }

    protected static String getValue(@Nullable DialogResponseView responseView, String key, String defaultValue) {
        if (responseView == null) return defaultValue;
        return Objects.requireNonNullElse(responseView.getText(key), defaultValue);
    }

    protected static boolean getValue(@Nullable DialogResponseView responseView, String key, boolean defaultValue) {
        if (responseView == null) return defaultValue;
        return Objects.requireNonNullElse(responseView.getBoolean(key), defaultValue);
    }
}
