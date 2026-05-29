package at.iamsoccer.soccerisawesome.itemrename.dialog;

import at.hugob.plugin.library.config.YamlFileConfig;
import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public interface IDialogFactory {
    DialogLike create(Player player);
    void reload(YamlFileConfig configFile, ConfigurationSection configSection);
    boolean hasPermission(Player player);
    Component externalTitle();
}
