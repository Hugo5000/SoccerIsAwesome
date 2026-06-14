package at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic;

import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.interfaces.IComponentParser;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
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

    public static UnparsedButtonInfo parseFromConfigSection(YamlFileConfig configFile, ConfigurationSection configSection, String configLocation, @Nullable String defaultLocation) {
        @Nullable UnparsedButtonInfo stringButtonInfo = parseFromConfigSection(configSection, configLocation);
        if (stringButtonInfo == null && defaultLocation != null)
            stringButtonInfo = parseFromConfigSection(configFile, defaultLocation);
        if (stringButtonInfo == null)
            stringButtonInfo = new UnparsedButtonInfo(configSection.getCurrentPath() + "." + configLocation, null);
        return stringButtonInfo;
    }

    public static @Nullable UnparsedButtonInfo parseFromConfigSection(ConfigurationSection configSection, String path) {
        if (configSection.isString(path)) {
            return new UnparsedButtonInfo(
                configSection.getString(path),
                null
            );
        }
        if (configSection.isConfigurationSection(path) && configSection.isString(path + ".label")) {
            return new UnparsedButtonInfo(
                configSection.getString(path + ".label"),
                configSection.isSet(path+".tooltip") && configSection.isString(path + ".tooltip") ? configSection.getString(path + ".tooltip") : null
            );
        }
        return null;
    }

    public record ButtonInfo(Component label, @Nullable Component tooltip) {
    }

    public record UnparsedButtonInfo(String label, @Nullable String tooltip) {
        public <User extends Audience> ButtonInfo parse(User user, IComponentParser<User> parser) {
            return new ButtonInfo(
                parser.parse(user, label),
                tooltip == null ? null : parser.parse(user, tooltip)
            );
        }

        public <User extends Audience> ButtonInfo parse(User user, IComponentParser<User> parser, TagResolver... resolvers) {
            TagResolver resolver = TagResolver.builder()
                .resolver(parser.tagResolver(user))
                .resolvers(resolvers)
                .build();
            return new ButtonInfo(
                parser.parse(label, resolver),
                tooltip == null ? null : parser.parse(tooltip, resolver)
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
