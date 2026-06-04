package at.iamsoccer.soccerisawesome.itemrename.dialog.templates;

import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.buttons.DialogButton;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.interfaces.IConfigSectionReloadable;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import net.kyori.adventure.audience.Audience;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractDialogButtonFactory<User extends Audience> implements IConfigSectionReloadable {
    protected final Class<User> userClass;
    private final @Nullable Supplier<AbstractDialogFactory<User>> returnDialogFactorySupplier;

    private final DialogButton<User> openButton;

    protected AbstractDialogButtonFactory(Class<User> userClass, @Nullable Supplier<AbstractDialogFactory<User>> returnDialogFactorySupplier) {
        this.userClass = userClass;
        this.returnDialogFactorySupplier = returnDialogFactorySupplier;
        this.openButton = newButton(this::openButtonInfo, "external", null, (response, user) -> {
            if (tryToOpenInternal(user)) {
                onExternalButtonPressed(response, user);
            }
        });
    }

    protected abstract void onExternalButtonPressed(DialogResponseView response, User user);

    protected @Nullable DialogButton.ButtonInfo openButtonInfo(User user) {
        return null;
    }

    @Override
    public void reload(YamlFileConfig configFile, ConfigurationSection configSection) {
        openButton.reload(configFile, configSection);
    }

    public final boolean isAllowedToOpen(Audience audience) {
        return userClass.isInstance(audience) && isAllowedToOpenInternal(userClass.cast(audience));
    }
    protected abstract boolean isAllowedToOpenInternal(User user);

    protected boolean tryToOpenInternal(User user) {
        return isAllowedToOpenInternal(user);
    }

    public final ActionButton openActionButton(User user) {
        return openButton.button(user);
    }

    public final ActionButton openActionButton(User user, @Range(from = 1, to = 1024) int width) {
        return openButton.button(user, width);
    }

    protected final void returnToPrevious(User user) {
        if (returnDialogFactorySupplier == null) return;
        if (!isAllowedToOpen(user)) return;
        returnDialogFactorySupplier.get().open(user);
    }

    protected final boolean hasReturnDialog() {
        return returnDialogFactorySupplier != null;
    }

    protected final DialogButton<User> newButton(String configLocation, @Nullable String defaultLocation, @Nullable DialogButton.IButtonCallback<User> callback) {
        return new DialogButton<>(userClass, configLocation, defaultLocation, callback);
    }

    protected final DialogButton<User> newButton(@Nullable DialogButton.IButtonInfoSupplier<User> buttonInfoOverwriteSupplier, String configLocation, @Nullable String defaultLocation, @Nullable DialogButton.IButtonCallback<User> callback) {
        return new DialogButton<>(userClass, buttonInfoOverwriteSupplier, configLocation, defaultLocation, callback);
    }
}
