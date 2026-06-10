package at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic;

import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.interfaces.IComponentParser;
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
public class DialogButton<User extends Audience> {
    public static final ClickCallback.Options UNLIMITED_CALLBACK_OPTIONS = ClickCallback.Options.builder().uses(-1).lifetime(ChronoUnit.FOREVER.getDuration()).build();
    private final Class<User> userClass;

    private final @Nullable IButtonInfoSupplier<User> buttonInfoOverwriteSupplier;

    private final @Nullable DialogAction action;

    public DialogButton(Class<User> userClass, IButtonInfoSupplier<User> buttonInfoOverwriteSupplier, @Nullable DialogButton.IButtonCallback<User> callback) {
        this.userClass = userClass;
        this.buttonInfoOverwriteSupplier = buttonInfoOverwriteSupplier;
        this.action = callback == null ? null : DialogAction.customClick((response, audience) -> {
            if (userClass.isInstance(audience)) {
                callback.accept(response, userClass.cast(audience));
            }
        }, UNLIMITED_CALLBACK_OPTIONS);
    }

    public final ActionButton button(User user) {
        return buttonBuilder(user).build();
    }

    public final ActionButton button(User user, @Range(from = 1, to = 1024) int width) {
        return buttonBuilder(user).width(width).build();
    }

    private ActionButton.Builder buttonBuilder(User user) {
        var buttonInfo = buttonInfoOverwriteSupplier.supplyFor(user);
        return ActionButton.builder(buttonInfo.label).tooltip(buttonInfo.tooltip).action(action);
    }

    public static <User extends Audience> UnparsedButtonInfo<User> parseFromConfigSection(YamlFileConfig configFile, ConfigurationSection configSection, String configLocation, @Nullable String defaultLocation) {
        @Nullable UnparsedButtonInfo<User> stringButtonInfo = parseFromConfigSection(configSection, configLocation);
        if (stringButtonInfo == null && defaultLocation != null)
            stringButtonInfo = parseFromConfigSection(configFile, defaultLocation);
        if (stringButtonInfo == null)
            stringButtonInfo = new UnparsedButtonInfo<>(configSection.getCurrentPath() + "." + configLocation, null);
        return stringButtonInfo;
    }

    public static <User extends Audience> @Nullable UnparsedButtonInfo<User> parseFromConfigSection(ConfigurationSection configSection, String path) {
        if (configSection.isString(path)) {
            return new UnparsedButtonInfo<>(
                configSection.getString(path),
                null
            );
        }
        if (configSection.isConfigurationSection(path) && configSection.isString(path + ".label")) {
            return new UnparsedButtonInfo<>(
                configSection.getString(path + ".label"),
                configSection.isString(path + ".tooltip") ? configSection.getString(path + ".tooltip") : null
            );
        }
        return null;
    }

    public record ButtonInfo(Component label, @Nullable Component tooltip) {
    }

    public record UnparsedButtonInfo<User extends Audience>(String label, @Nullable String tooltip) {
        public ButtonInfo parse(IComponentParser<User> parser, User user, @Nullable DialogResponseView response) {
            return new ButtonInfo(
                parser.parse(user, label, response),
                tooltip == null ? null : parser.parse(user, tooltip, response)
            );
        }
    }

    @FunctionalInterface
    public interface IButtonCallback<User extends Audience> {
        void accept(DialogResponseView response, User user);
    }

    @FunctionalInterface
    public interface IButtonInfoSupplier<User extends Audience> {
        ButtonInfo supplyFor(User user);
    }
}
