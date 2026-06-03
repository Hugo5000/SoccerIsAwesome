package at.iamsoccer.soccerisawesome.itemrename.dialog.templates;

import at.hugob.plugin.library.config.YamlFileConfig;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.dialog.DialogLike;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractPreviewAndApplyEditorDialog extends AbstractBasicDialogFactory {
    public AbstractPreviewAndApplyEditorDialog(
        Permission permission,
        @Nullable Supplier<IDialogFactory> returnDialogFactorySupplier
    ) {
        super(permission, returnDialogFactorySupplier);
    }

    private final DialogButton applyButton = new DialogButton("apply", "dialog.default.apply", (response, audience) -> {
        if (!(audience instanceof Player player) || !hasPermission(player)) return;
        onApply(response, player);
        returnToPrevious(audience);
    });
    private final DialogButton previewButton = new DialogButton("preview", "dialog.default.preview", (response, audience) -> {
        if (!(audience instanceof Player player) || !hasPermission(player)) return;
        onPreview(response, player);
        audience.showDialog(create(player, response));
    });

    @Override
    public DialogLike create(Player player) {
        return create(player, null);
    }

    protected DialogLike create(Player player, @Nullable DialogResponseView responseView) {
        return createDialog(body -> this.body(body, player, responseView), inputs(player, responseView), closeButton -> DialogType.multiAction(List.of(
                previewButton.button(player),
                applyButton.button(player)
            ), closeButton.button(player), 1)
        );
    }

    protected abstract List<DialogBody> body(List<DialogBody> body, Player player, @Nullable DialogResponseView responseView);
    protected abstract List<DialogInput> inputs(Player player, @Nullable DialogResponseView responseView);

    protected abstract void onApply(DialogResponseView response, Player player);
    protected abstract void onPreview(DialogResponseView response, Player player);

    @Override
    public void reload(YamlFileConfig configFile, ConfigurationSection configSection) {
        super.reload(configFile, configSection);
        applyButton.reload(configFile, configSection);
        previewButton.reload(configFile, configSection);
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
