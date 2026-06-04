package at.iamsoccer.soccerisawesome.itemrename.dialog.templates.buttons;

import at.hugob.plugin.library.config.ConfigUtils;
import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.interfaces.IConfigSectionReloadable;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.time.temporal.ChronoUnit;

@SuppressWarnings("UnstableApiUsage")
public class DialogButton<User extends Audience> implements IConfigSectionReloadable {
    public static final ClickCallback.Options UNLIMITED_CALLBACK_OPTIONS = ClickCallback.Options.builder().uses(-1).lifetime(ChronoUnit.FOREVER.getDuration()).build();
    private final Class<User> userClass;

    private final @Nullable IButtonInfoSupplier<User> buttonInfoOverwriteSupplier;

    private final @Nullable DialogAction action;
    private final String configLocation;
    private final @Nullable String defaultLocation;

    private Component label = Component.empty();
    private @Nullable Component tooltip;

    public DialogButton(Class<User> userClass, String configLocation, @Nullable String defaultLocation,  @Nullable DialogButton.IButtonCallback<User> callback) {
        this(userClass, null, configLocation, defaultLocation, callback);
    }

    public DialogButton(Class<User> userClass, @Nullable IButtonInfoSupplier<User> buttonInfoOverwriteSupplier, String configLocation, @Nullable String defaultLocation, @Nullable DialogButton.IButtonCallback<User> callback) {
        this.userClass = userClass;
        this.buttonInfoOverwriteSupplier = buttonInfoOverwriteSupplier;
        this.action = callback == null ? null : DialogAction.customClick((response, audience) -> {
            if (userClass.isInstance(audience)) {
                callback.accept(response, userClass.cast(audience));
            }
        }, UNLIMITED_CALLBACK_OPTIONS);
        this.configLocation = configLocation;
        this.defaultLocation = defaultLocation;
    }

    public final ActionButton button(User user) {
        return buttonBuilder(user).build();
    }

    public final ActionButton button(User user, @Range(from = 1, to = 1024) int width) {
        return buttonBuilder(user).width(width).build();
    }

    private ActionButton.Builder buttonBuilder(User user) {
        if (buttonInfoOverwriteSupplier != null) {
            @Nullable var buttonInfo = buttonInfoOverwriteSupplier.supplyFor(user);
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

    @FunctionalInterface
    public interface IButtonCallback<User extends Audience> {
        void accept(DialogResponseView response, User user);
    }

    @FunctionalInterface
    public interface IButtonInfoSupplier<User extends Audience> {
        @Nullable ButtonInfo supplyFor(User user);
    }
}
