package at.iamsoccer.soccerisawesome.itemrename.dialog.templates;

import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.buttons.DialogButton;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.dialog.DialogLike;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractPreviewAndApplyEditorDialog<User extends Audience> extends AbstractDialogFactory<User> {
    public AbstractPreviewAndApplyEditorDialog(
        Class<User> userClass,
        @Nullable Supplier<AbstractDialogFactory<User>> returnDialogFactorySupplier
    ) {
        super(userClass, returnDialogFactorySupplier);
    }

    private final DialogButton<User> applyButton = newButton( "apply", "dialog.default.apply", (response, user) -> {
        if(!tryToOpenInternal(user)) return;
        onApply(response, user);
        returnToPrevious(user);
    });
    private final DialogButton<User> previewButton = newButton("preview", "dialog.default.preview", (response, user) -> {
        if(!tryToOpenInternal(user)) return;
        onPreview(response, user);
    });

    @Override
    protected void open(@Nullable DialogResponseView response, User user) {
        if (!tryToOpenInternal(user)) return;
        user.showDialog(create(user, response));
    }

    protected void addButtons(User user, List<ActionButton> buttons) {
    }

    protected DialogLike create(User user, @Nullable DialogResponseView responseView) {
        var buttons = new ArrayList<>(List.of(
            previewButton.button(user),
            applyButton.button(user)
        ));
        addButtons(user, buttons);
        return createDialog(body -> this.body(body, user, responseView), inputs(user, responseView),
            closeButton -> DialogType.multiAction(buttons, closeButton.button(user), 1)
        );
    }

    protected abstract List<DialogBody> body(List<DialogBody> body, User user, @Nullable DialogResponseView responseView);
    protected abstract List<DialogInput> inputs(User user, @Nullable DialogResponseView responseView);

    protected abstract void onApply(DialogResponseView response, User user);

    protected void onPreview(DialogResponseView response, User user) {
        open(response, user);
    }

    @Override
    public void reload(YamlFileConfig configFile, ConfigurationSection configSection) {
        super.reload(configFile, configSection);
        applyButton.reload(configFile, configSection);
        previewButton.reload(configFile, configSection);
    }

    protected static Float getValue(@Nullable DialogResponseView responseView, String key, float defaultValue) {
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
