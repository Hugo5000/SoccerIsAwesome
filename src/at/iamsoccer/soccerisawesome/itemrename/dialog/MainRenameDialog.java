package at.iamsoccer.soccerisawesome.itemrename.dialog;

import at.hugob.plugin.library.config.ConfigUtils;
import at.hugob.plugin.library.config.YamlFileConfig;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static at.iamsoccer.soccerisawesome.itemrename.dialog.rename.AbstractRenameDialog.UNLIMITED_CALLBACK_OPTIONS;

public class MainRenameDialog {
    private final List<IDialogFactory> dialogs;

    private final List<DialogActionInfo> dialogActionInfos;

    private Component title = Component.empty();
    private List<Component> info = Collections.emptyList();
    // Button Labels
    private Component cancel = Component.empty();

    public MainRenameDialog(IDialogFactory... dialogs) {
        this.dialogs = Arrays.stream(dialogs).toList();
        dialogActionInfos = this.dialogs.stream()
            .map(dialog -> new DialogActionInfo(dialog, DialogAction.customClick((response, audience) -> {
                if (!(audience instanceof Player player) || !dialog.hasPermission(player)) return;
                player.showDialog(dialog.create(player));
            }, UNLIMITED_CALLBACK_OPTIONS))).toList();
    }

    public DialogLike create(Player player) {
        var body = info.stream()
            .map(text -> DialogBody.plainMessage(text))
            .toList();
        var inputs = dialogActionInfos.stream()
            .filter(dialogInfo -> dialogInfo.factory().hasPermission(player))
            .map(dialogInfo -> ActionButton.builder(dialogInfo.factory.externalTitle()).action(dialogInfo.action).build())
            .toList();

        return Dialog.create(builder ->
            builder.empty().base(DialogBase.builder(title)
                    .body(body)
                    .inputs(List.of())
                    .canCloseWithEscape(true)
                    .build())
                .type(DialogType.multiAction(inputs, ActionButton.builder(cancel).build(), 1))
        );
    }

    public void reload(YamlFileConfig configFile, ConfigurationSection configSection) {
        title = ConfigUtils.parseComponent(configFile, configSection.getString("title"), null, null);
        info = ConfigUtils.parseComponentList(configFile, configSection.getStringList("info"), null, null);
        cancel = ConfigUtils.parseComponent(configFile, configSection.getString("cancel"), null, null);
    }

    private record DialogActionInfo(
        IDialogFactory factory,
        DialogAction action
    ) {
    }
}
