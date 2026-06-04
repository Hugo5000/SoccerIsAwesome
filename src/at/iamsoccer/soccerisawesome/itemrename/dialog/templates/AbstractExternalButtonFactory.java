package at.iamsoccer.soccerisawesome.itemrename.dialog.templates;

import at.hugob.plugin.library.config.ConfigUtils;
import at.hugob.plugin.library.config.YamlFileConfig;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.action.DialogActionCallback;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.function.Function;
import java.util.function.Supplier;

import static at.iamsoccer.soccerisawesome.itemrename.dialog.rename.AbstractRenameDialog.UNLIMITED_CALLBACK_OPTIONS;

@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractExternalButtonFactory implements IActionButtonFactory, IPermissible, IConfigSectionReloadable {
    private final @Nullable Supplier<IDialogFactory> returnDialogFactorySupplier;
    protected final @Nullable Permission permission;

    private final DialogButton openButton = new DialogButton(this::openButtonInfo, "external", null, (response, audience) -> {
        if (!(audience instanceof Player player) || !hasPermission(player)) return;
        onClick(player);
    });

    protected abstract void onClick(Player player);

    protected @Nullable DialogButton.ButtonInfo openButtonInfo(Player player) {
        return null;
    }

    protected AbstractExternalButtonFactory(
        @Nullable Permission permission, @Nullable Supplier<IDialogFactory> returnDialogFactorySupplier
    ) {
        this.permission = permission;
        this.returnDialogFactorySupplier = returnDialogFactorySupplier;
    }

    @Override
    public boolean hasPermission(Player player) {
        if (permission == null) return true;
        return player.hasPermission(permission);
    }

    @Override
    public final ActionButton openActionButton(Player player) {
        return openButton.button(player);
    }

    @Override
    public final ActionButton openActionButton(Player player, @Range(from = 1, to = 1024) int width) {
        return openButton.button(player, width);
    }

    @Override
    public final @Nullable DialogAction openAction() {
        return openButton.action;
    }

    protected void returnToPrevious(Audience audience) {
        if (returnDialogFactorySupplier == null) return;
        if (!(audience instanceof Player player) || !hasPermission(player) || !returnDialogFactorySupplier.get().hasPermission(player))
            return;
        player.showDialog(returnDialogFactorySupplier.get().create(player));
    }

    @Override
    public void reload(YamlFileConfig configFile, ConfigurationSection configSection) {
        openButton.reload(configFile, configSection);
    }

    protected boolean hasReturnDialog() {
        return returnDialogFactorySupplier != null;
    }

    public static class DialogButton implements IConfigSectionReloadable {
        private final @Nullable Function<Player, @Nullable ButtonInfo> buttonInfoOverwriteSupplier;

        private final @Nullable DialogAction action;
        private final String configLocation;
        private final @Nullable String defaultLocation;

        private Component label = Component.empty();
        private @Nullable Component tooltip;

        public DialogButton(String configLocation, @Nullable String defaultLocation, @Nullable DialogActionCallback callback) {
            this(null, configLocation, defaultLocation, callback);
        }

        public DialogButton(@Nullable Function<Player, @Nullable ButtonInfo> buttonInfoOverwriteSupplier, String configLocation, @Nullable String defaultLocation, @Nullable DialogActionCallback callback) {
            this.buttonInfoOverwriteSupplier = buttonInfoOverwriteSupplier;
            this.action = callback == null ? null : DialogAction.customClick(callback, UNLIMITED_CALLBACK_OPTIONS);
            this.configLocation = configLocation;
            this.defaultLocation = defaultLocation;
        }

        public final ActionButton button(Player player) {
            return buttonBuilder(player).build();
        }

        public final ActionButton button(Player player, @Range(from = 1, to = 1024) int width) {
            return buttonBuilder(player).width(width).build();
        }

        private ActionButton.Builder buttonBuilder(Player player) {
            if (buttonInfoOverwriteSupplier != null) {
                @Nullable var buttonInfo = buttonInfoOverwriteSupplier.apply(player);
                if (buttonInfo != null) {
                    return ActionButton.builder(buttonInfo.label).tooltip(buttonInfo.tooltip).action(action);
                }
            }
            return ActionButton.builder(label).tooltip(tooltip).action(action);
        }

        @Override
        public void reload(YamlFileConfig configFile, ConfigurationSection configSection) {
            if (parseFromConfigSection(configFile, configSection, configLocation)) return;
            if (defaultLocation != null) {
                if (parseFromConfigSection(configFile, configFile, defaultLocation)) return;
            }
            label = Component.text(configSection.getCurrentPath() + "." + configLocation);
            tooltip = null;
        }

        private boolean parseFromConfigSection(YamlFileConfig configFile, ConfigurationSection configSection, String path) {
            if (configSection.isString(path)) {
                label = ConfigUtils.parseComponent(configFile, configSection.getString(path), null, null);
                tooltip = null;
                return true;
            }
            if (configSection.isConfigurationSection(path) && configSection.isString(path + ".label")) {
                label = ConfigUtils.parseComponent(configFile, configSection.getString(path + ".label"), null, null);
                tooltip = configSection.isString(path + ".tooltip") ? ConfigUtils.parseComponent(configFile, configSection.getString(path + ".tooltip"), null, null) : null;
                return true;
            }
            return false;
        }

        public record ButtonInfo(Component label, @Nullable Component tooltip) {
        }
    }
}
