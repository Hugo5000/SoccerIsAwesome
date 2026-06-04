package at.iamsoccer.soccerisawesome.itemrename.dialog.templates;

import at.hugob.plugin.library.config.ConfigUtils;
import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.buttons.DialogButton;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractDialogFactory<User extends Audience> extends AbstractDialogButtonFactory<User> {
    private Component title = Component.empty();
    private List<DialogBody> infoFields = Collections.emptyList();

    private final DialogButton<User> closeButton = newButton("close", "dialog.default.close", (response, audience) -> {
        returnToPrevious(audience);
    });

    protected AbstractDialogFactory(Class<User> userClass, @Nullable Supplier<AbstractDialogFactory<User>> returnDialogFactorySupplier) {
        super(userClass, returnDialogFactorySupplier);
    }

    @Override
    protected void onExternalButtonPressed(DialogResponseView response, User user) {
        open(response, user);
    }

    public final void open(User user) {
        open(null, user);
    }

    protected abstract void open(@Nullable DialogResponseView response, User user);

    protected @Nullable Component titleProvider() {
        return null;
    }

    @Override
    public void reload(YamlFileConfig configFile, ConfigurationSection configSection) {
        super.reload(configFile, configSection);
        this.title = Objects.requireNonNullElseGet(
            titleProvider(),
            () -> ConfigUtils.parseComponent(configFile, Objects.requireNonNullElse(configSection.getString("title"), configSection.getCurrentPath() + ".title"), null, null)
        );
        infoFields = ConfigUtils.parseComponentList(configFile, configSection.getStringList("info"), null, null).stream()
            .map(DialogBody::plainMessage)
            .map(DialogBody.class::cast)
            .toList();

        closeButton.reload(configFile, configSection);
    }

    protected DialogLike createDialog(
        @Nullable Function<List<DialogBody>, List<DialogBody>> bodySupplier,
        List<? extends DialogInput> inputs,
        Function<DialogButton<User>, DialogType> typeSupplier
    ) {
        return createDialog(title, bodySupplier, inputs, typeSupplier);
    }

    protected DialogLike createDialog(
        Component title,
        @Nullable Function<List<DialogBody>, List<DialogBody>> bodySupplier,
        List<? extends DialogInput> inputs,
        Function<DialogButton<User>, DialogType> typeSupplier
    ) {
        return Dialog.create(builder ->
            builder.empty()
                .base(DialogBase.builder(title)
                    .body(bodySupplier == null ? infoFields : bodySupplier.apply(new ArrayList<>(infoFields)))
                    .inputs(inputs)
                    .pause(false)
                    .afterAction(hasReturnDialog() ? DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE : DialogBase.DialogAfterAction.CLOSE)
                    .build())
                .type(typeSupplier.apply(closeButton))
        );
    }
}
