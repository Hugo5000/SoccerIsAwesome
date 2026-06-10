package at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic;

import at.hugob.plugin.library.config.ConfigUtils;
import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.interfaces.IConfigSectionReloadable;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractConfigDialogButtonFactory<User extends Audience> extends AbstractDialogButtonFactory<User> implements IConfigSectionReloadable {
    private HashMap<String, DialogButton.UnparsedButtonInfo<User>> buttonInfos;
    private YamlFileConfig config;

    public AbstractConfigDialogButtonFactory(Class<User> userClass, @Nullable Supplier<AbstractDialogFactory<User>> returnFactorySupplier) {
        super(userClass, returnFactorySupplier);
        if(buttonInfos == null) buttonInfos = new HashMap<>();
    }

    @Override
    public void reload(YamlFileConfig configFile, ConfigurationSection configSection) {
        this.config = configFile;

        buttonInfos.entrySet().forEach(entry -> entry.setValue(DialogButton.parseFromConfigSection(config, configSection, entry.getKey(), "dialog.default." + entry.getKey())));
    }

    @Override
    public Component parse(String string, TagResolver resolver) {
        return ConfigUtils.parseComponent(config, string, resolver, null);
    }

    @Override
    protected DialogButton.IButtonInfoSupplier<User> buttonInfoSupplier(String name) {
        if (buttonInfos == null) buttonInfos = new HashMap<>();
        buttonInfos.put(name, new DialogButton.UnparsedButtonInfo<>(name, null));
        return user -> buttonInfos.get(name).parse(this, user, null);
    }

}
