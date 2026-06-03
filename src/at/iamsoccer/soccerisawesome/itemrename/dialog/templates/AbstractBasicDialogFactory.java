package at.iamsoccer.soccerisawesome.itemrename.dialog.templates;

import at.hugob.plugin.library.config.ConfigUtils;
import at.hugob.plugin.library.config.YamlFileConfig;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractBasicDialogFactory extends AbstractExternalButtonFactory implements IDialogFactory {
    private Component title = Component.empty();
    private List<DialogBody> infoFields = Collections.emptyList();

    private final DialogButton closeButton = new DialogButton("close", "dialog.default.close", (response, audience) -> {
        returnToPrevious(audience);
    });

    protected AbstractBasicDialogFactory(Permission permission, @Nullable Supplier<IDialogFactory> returnDialogFactorySupplier) {
        super(permission, returnDialogFactorySupplier);
    }

    protected @Nullable Component titleProvider() {
        return null;
    }

    @Override
    protected void onClick(Player player) {
        player.showDialog(create(player));
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

    protected DialogLike createDialog(@Nullable Function<List<DialogBody>, List<DialogBody>> bodySupplier, List<? extends DialogInput> inputs, Function<DialogButton, DialogType> typeSupplier) {
        return createDialog(title, bodySupplier, inputs, typeSupplier);
    }

    protected DialogLike createDialog(Component title, @Nullable Function<List<DialogBody>, List<DialogBody>> bodySupplier, List<? extends DialogInput> inputs, Function<DialogButton, DialogType> typeSupplier) {
        return Dialog.create(builder ->
            builder.empty()
                .base(DialogBase.builder(title)
                    .body(bodySupplier == null ? infoFields : bodySupplier.apply(new ArrayList<>(infoFields)))
                    .inputs(inputs)
                    .pause(false)
                    .afterAction(DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE)
                    .build())
                .type(typeSupplier.apply(closeButton))
        );
    }
}
