package at.iamsoccer.soccerisawesome.itemrename.dialog.special;

import at.hugob.plugin.library.config.ConfigUtils;
import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.itemrename.IConfigSectionReloadable;
import at.iamsoccer.soccerisawesome.itemrename.dialog.IDialogFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.IExternalDialogFactory;
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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.checkerframework.checker.index.qual.Positive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static at.iamsoccer.soccerisawesome.itemrename.dialog.rename.AbstractRenameDialog.UNLIMITED_CALLBACK_OPTIONS;

public abstract class AbstractButtonListDialog implements IExternalDialogFactory, IConfigSectionReloadable {
    public final Permission permission;
    private final Supplier<IDialogFactory> returnFactorySupplier;

    private Component externalTitle = Component.empty();
    private Component title = Component.empty();
    private List<Component> info = Collections.emptyList();
    // Button Labels
    private Component cancel = Component.empty();

    public AbstractButtonListDialog(Permission permission, Supplier<IDialogFactory> returnFactorySupplier) {
        this.permission = permission;
        this.returnFactorySupplier = returnFactorySupplier;
    }

    private final DialogAction openAction = DialogAction.customClick(this::onOpen, UNLIMITED_CALLBACK_OPTIONS);
    private final DialogAction cancelAndReturn = DialogAction.customClick(this::onCancelAndReturn, UNLIMITED_CALLBACK_OPTIONS);

    @Override
    public DialogLike create(Player player, boolean returnToMain) {
        var item = player.getInventory().getItemInMainHand().clone();

        List<DialogBody> body = new ArrayList<>(info.size() + 1);
        info.stream()
            .map(text -> DialogBody.plainMessage(text))
            .forEach(body::add);
        body.add(DialogBody.item(item).build());
        return Dialog.create(builderFactory -> {
                builderFactory.empty()
                    .base(DialogBase.builder(title)
                        .body(body)
                        .inputs(List.of())
                        .afterAction(DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE)
                        .pause(false)
                        .build())
                    .type(DialogType.multiAction(getDialogButtons(player, item))
                        .columns(getColumns())
                        .exitAction(ActionButton.builder(cancel).action(cancelAndReturn).build())
                        .build()
                    );
            }
        );
    }

    @Positive
    protected int getColumns() {
        return 1;
    }

    protected abstract List<ActionButton> getDialogButtons(Player player, ItemStack item);

    @Override
    public void reload(YamlFileConfig configFile, ConfigurationSection configSection) {
        externalTitle = ConfigUtils.parseComponent(configFile, configSection.getString("external-title"), null, null);
        title = ConfigUtils.parseComponent(configFile, configSection.getString("title"), null, null);
        info = ConfigUtils.parseComponentList(configFile, configSection.getStringList("info"), null, null);
        cancel = ConfigUtils.parseComponent(configFile, configSection.getString("cancel"), null, null);
    }

    @Override
    public boolean hasPermission(Player player) {
        return player.hasPermission(permission);
    }

    @Override
    public ActionButton actionButton() {
        return ActionButton.builder(externalTitle).action(openAction).build();
    }

    @Override
    public DialogAction dialogAction() {
        return openAction;
    }

    private void onOpen(DialogResponseView response, Audience audience) {
        if (!(audience instanceof Player player) || !player.hasPermission(permission)) return;
        player.showDialog(create(player, true));
    }

    private void onCancelAndReturn(DialogResponseView response, Audience audience) {
        if (!(audience instanceof Player player) || !player.hasPermission(permission) || !returnFactorySupplier.get().hasPermission(player))
            return;
        player.showDialog(returnFactorySupplier.get().create(player, true));
    }
}
