package at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic;

import at.hugob.plugin.library.config.ConfigUtils;
import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.interfaces.IConfigSectionReloadable;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class ConfigDialogFactory<User extends Audience> extends AbstractDialogFactory<User> implements IConfigSectionReloadable {
    private HashMap<String, DialogButton.UnparsedButtonInfo<User>> buttonInfos;
    private YamlFileConfig config;
    private String title = "";
    private List<String> infos = Collections.emptyList();

    public ConfigDialogFactory(Class<User> userClass, @Nullable Supplier<AbstractDialogFactory<User>> returnFactorySupplier) {
        super(userClass, returnFactorySupplier);
        if(buttonInfos == null) buttonInfos = new HashMap<>();
    }

    @Override
    protected DialogButton.IButtonInfoSupplier<User> buttonInfoSupplier(String name) {
        if(buttonInfos == null) buttonInfos = new HashMap<>(); // needs to be done bc loading shittery
        buttonInfos.put(name, new DialogButton.UnparsedButtonInfo<>(name, null));
        return user -> buttonInfos.get(name).parse(this, user, null);
    }

    @Override
    public void reload(YamlFileConfig configFile, ConfigurationSection configSection) {
        this.config = configFile;

        this.title = Objects.requireNonNullElse(configSection.getString("title"), configSection.getCurrentPath() + ".title");
        this.infos = configSection.getStringList("info");

        buttonInfos.entrySet().forEach(entry -> entry.setValue(DialogButton.parseFromConfigSection(config, configSection, entry.getKey(), "dialog.default." + entry.getKey())));
    }

    @Override
    public Component parse(String string, TagResolver resolver) {
        return ConfigUtils.parseComponent(config, string, resolver, null);
    }

    @Override
    protected Component dialogTitle(User user, @Nullable DialogResponseView response) {
        return parse(user, title, response);
    }

    @Override
    protected List<DialogBody> dialogBody(User user, @Nullable DialogResponseView response) {
        return parse(user, infos.stream(), response)
            .map(DialogBody::plainMessage)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    protected List<DialogInput> dialogInputs(User user, @Nullable DialogResponseView response) {
        return new ArrayList<>();
    }

    protected DialogType dialogType(DialogButton<User> closeButton, User user, @Nullable DialogResponseView response) {
        return DialogType.notice(closeButton.button(user));
    }
}
