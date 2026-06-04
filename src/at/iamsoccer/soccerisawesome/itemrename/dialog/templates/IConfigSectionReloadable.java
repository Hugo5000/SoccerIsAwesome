package at.iamsoccer.soccerisawesome.itemrename.dialog.templates;

import at.hugob.plugin.library.config.YamlFileConfig;
import org.bukkit.configuration.ConfigurationSection;

public interface IConfigSectionReloadable {
    void reload(YamlFileConfig configFile, ConfigurationSection configSection);
}
