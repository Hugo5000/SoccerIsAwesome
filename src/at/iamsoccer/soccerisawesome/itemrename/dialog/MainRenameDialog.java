package at.iamsoccer.soccerisawesome.itemrename.dialog;

import at.hugob.plugin.library.config.ConfigUtils;
import at.hugob.plugin.library.config.YamlFileConfig;
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
import org.bukkit.permissions.Permission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static at.iamsoccer.soccerisawesome.itemrename.dialog.rename.AbstractRenameDialog.UNLIMITED_CALLBACK_OPTIONS;

public class MainRenameDialog implements IDialogFactory {
    private final List<IExternalDialogFactory> dialogs;
    private final Permission permission;

    private Component title = Component.empty();
    private List<Component> info = Collections.emptyList();
    // Button Labels
    private Component cancel = Component.empty();

    private final DialogAction cancelAction = DialogAction.customClick(this::onCancel, UNLIMITED_CALLBACK_OPTIONS);

    private void onCancel(DialogResponseView dialogResponseView, Audience audience) {
        audience.closeDialog();
    }

    public MainRenameDialog(Permission permission, IExternalDialogFactory... dialogs) {
        this.permission = permission;
        this.dialogs = Arrays.stream(dialogs).toList();
    }

    @Override
    public DialogLike create(Player player, boolean returnToMain) {
        var item = player.getInventory().getItemInMainHand();
        var body = new ArrayList<DialogBody>(info.size() + 1);
        info.stream()
            .map(text -> DialogBody.plainMessage(text))
            .forEach(body::add);
        body.add(DialogBody.item(item).showDecorations(true).build());
        var inputs = dialogs.stream()
            .filter(dialogInfo -> dialogInfo.hasPermission(player))
            .map(IActionButtonFactory::actionButton)
            .toList();
        return Dialog.create(builder ->
            builder.empty().base(DialogBase.builder(title)
                    .body(body)
                    .inputs(List.of())
                    .canCloseWithEscape(true)
                    .pause(false)
                    .afterAction(DialogBase.DialogAfterAction.NONE)
                    .build())
                .type(DialogType.multiAction(inputs, ActionButton.builder(cancel).action(cancelAction).build(), 1))
        );
    }

    public void reload(YamlFileConfig configFile, ConfigurationSection configSection) {
        title = ConfigUtils.parseComponent(configFile, configSection.getString("title"), null, null);
        info = ConfigUtils.parseComponentList(configFile, configSection.getStringList("info"), null, null);
        cancel = ConfigUtils.parseComponent(configFile, configSection.getString("cancel"), null, null);
    }

    @Override
    public boolean hasPermission(Player player) {
        return player.hasPermission(permission);
    }

    private record DialogActionInfo(
        IExternalDialogFactory factory,
        DialogAction action
    ) {
    }
}
