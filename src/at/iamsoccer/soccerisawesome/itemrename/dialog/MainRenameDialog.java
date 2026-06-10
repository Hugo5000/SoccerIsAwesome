package at.iamsoccer.soccerisawesome.itemrename.dialog;

import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.itemrename.dialog.component.DataComponentListDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.component.copy.CopyComponentDisplayDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.rename.ItemCustomNameRenameDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.rename.ItemLoreRenameDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.rename.ItemNameRenameDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.AbstractDialogButtonFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.AbstractDialogFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractButtonListDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.tooltip.TooltipDisplayDialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class MainRenameDialog extends AbstractButtonListDialog {
    private final ItemCustomNameRenameDialog itemCustomNameRenameDialog = new ItemCustomNameRenameDialog(Bukkit.getServer().getPluginManager().getPermission("shia.rename.custom-name"), () -> this);
    private final ItemNameRenameDialog itemNameRenameDialog = new ItemNameRenameDialog(Bukkit.getServer().getPluginManager().getPermission("shia.rename.item-name"), () -> this);
    private final ItemLoreRenameDialog itemLoreRenameDialog = new ItemLoreRenameDialog(Bukkit.getServer().getPluginManager().getPermission("shia.rename.lore"), () -> this);
    private final TooltipDisplayDialog tooltipDisplayDialog = new TooltipDisplayDialog(Bukkit.getServer().getPluginManager().getPermission("shia.rename.tooltip"), () -> this);
    private final DataComponentListDialog toggleableComponentsDialog = new DataComponentListDialog(Bukkit.getServer().getPluginManager().getPermission("shia.rename.component"), () -> this, (type, item, buttonFactory) -> !(buttonFactory instanceof AbstractDialogFactory), 1);
    private final DataComponentListDialog addComponentsDialog = new DataComponentListDialog(Bukkit.getServer().getPluginManager().getPermission("shia.rename.component"), () -> this, (type, item, buttonFactory) -> !item.hasData(type));
    private final DataComponentListDialog editComponentsDialog = new DataComponentListDialog(Bukkit.getServer().getPluginManager().getPermission("shia.rename.component"), () -> this, (type, item, buttonFactory) -> item.hasData(type));
    private final CopyComponentDisplayDialog copyComponentsDialog = new CopyComponentDisplayDialog(Bukkit.getServer().getPluginManager().getPermission("shia.rename.copy"), () -> this);

    public MainRenameDialog(Permission permission) {
        super(null, null);
    }

    public Stream<AbstractDialogButtonFactory<Player>> dialogFactories() {
        return Stream.of(
            itemNameRenameDialog,
            itemCustomNameRenameDialog,
            itemLoreRenameDialog,
            tooltipDisplayDialog,
            toggleableComponentsDialog,
            addComponentsDialog,
            editComponentsDialog,
            copyComponentsDialog
        );
    }

    @Override
    public void reload(YamlFileConfig configFile, ConfigurationSection configSection) {
        super.reload(configFile, configSection);
        itemNameRenameDialog.reload(configFile, configFile.getConfigurationSection("dialog.item-name"));
        itemCustomNameRenameDialog.reload(configFile, configFile.getConfigurationSection("dialog.custom-name"));
        itemLoreRenameDialog.reload(configFile, configFile.getConfigurationSection("dialog.lore"));
        tooltipDisplayDialog.reload(configFile, configFile.getConfigurationSection("dialog.tooltip"));
        toggleableComponentsDialog.reload(configFile, configFile.getConfigurationSection("dialog.toggles"));
        addComponentsDialog.reload(configFile, configFile.getConfigurationSection("dialog.component.add-list"));
        editComponentsDialog.reload(configFile, configFile.getConfigurationSection("dialog.component.edit-list"));
        copyComponentsDialog.reload(configFile, configFile.getConfigurationSection("dialog.component.copy"));
    }

    @Override
    protected List<ActionButton> getDialogButtons(Player player) {
        return dialogFactories()
            .filter(factory -> factory.isAllowedToOpen(player))
            .map(factory -> factory.externalButton().button(player))
            .toList();
    }
}
