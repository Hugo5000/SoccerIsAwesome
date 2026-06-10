package at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic;

import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.interfaces.IComponentParser;
import io.papermc.paper.dialog.DialogResponseView;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractDialogButtonFactory<User extends Audience> implements IComponentParser<User> {
    protected final Class<User> userClass;
    private final @Nullable Supplier<AbstractDialogFactory<User>> returnDialogFactorySupplier;

    private final DialogButton<User> openButton;

    protected AbstractDialogButtonFactory(Class<User> userClass, @Nullable Supplier<AbstractDialogFactory<User>> returnDialogFactorySupplier) {
        this.userClass = userClass;
        this.returnDialogFactorySupplier = returnDialogFactorySupplier;
        this.openButton = newButton("external", (response, user) -> {
            if (tryOpen(user)) {
                onExternalButtonPressed(response, user);
            }
        });
    }

    /**
     * The method called when the external button is pressed in a different Dialog
     *
     * @param response the Response View of the other dialog
     * @param user     the user who pressed the button
     */
    protected abstract void onExternalButtonPressed(DialogResponseView response, User user);

    public boolean isAllowedToOpen(User user) {
        return userClass.isInstance(user);
    }

    protected boolean tryOpen(User user) {
        return isAllowedToOpen(user);
    }

    public final DialogButton<User> externalButton() {
        return openButton;
    }

    protected final void returnToPrevious(User user) {
        if (returnDialogFactorySupplier == null) return;
        if (!isAllowedToOpen(user)) return;
        returnDialogFactorySupplier.get().open(user);
    }

    protected final boolean hasReturnDialog() {
        return returnDialogFactorySupplier != null;
    }

    protected final DialogButton<User> newButton(String name, @Nullable DialogButton.IButtonCallback<User> callback) {
        return new DialogButton<>(userClass, buttonInfoSupplier(name), callback);
    }

    protected abstract DialogButton.IButtonInfoSupplier<User> buttonInfoSupplier(String name);

    protected static Float getFloat(@Nullable DialogResponseView responseView, String key, Supplier<Float> defaultValue) {
        if (responseView == null) return defaultValue.get();
        return Objects.requireNonNullElseGet(responseView.getFloat(key), defaultValue);
    }

    protected static String getString(@Nullable DialogResponseView responseView, String key, Supplier<String> defaultValue) {
        if (responseView == null) return defaultValue.get();
        return Objects.requireNonNullElseGet(responseView.getText(key), defaultValue);
    }

    protected static boolean getBoolean(@Nullable DialogResponseView responseView, String key, Supplier<Boolean> defaultValue) {
        if (responseView == null) return defaultValue.get();
        return Objects.requireNonNullElseGet(responseView.getBoolean(key), defaultValue);
    }
}
