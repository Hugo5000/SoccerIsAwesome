package at.iamsoccer.soccerisawesome.itemrename.dialog;

import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.itemrename.dialog.component.DataComponentListDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.rename.ItemCustomNameRenameDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.rename.ItemLoreRenameDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.rename.ItemNameRenameDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractBasicDialogFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractButtonListDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractExternalButtonFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.tooltip.TooltipDisplayDialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;

import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class MainRenameDialog extends AbstractButtonListDialog {
    private final ItemCustomNameRenameDialog itemCustomNameRenameDialog = new ItemCustomNameRenameDialog(Bukkit.getServer().getPluginManager().getPermission("shia.rename.custom-name.command"), () -> this);
    private final ItemNameRenameDialog itemNameRenameDialog = new ItemNameRenameDialog(Bukkit.getServer().getPluginManager().getPermission("shia.rename.item-name"), () -> this);
    private final ItemLoreRenameDialog itemLoreRenameDialog = new ItemLoreRenameDialog(Bukkit.getServer().getPluginManager().getPermission("shia.rename.lore"), () -> this);
    private final TooltipDisplayDialog tooltipDisplayDialog = new TooltipDisplayDialog(Bukkit.getServer().getPluginManager().getPermission("shia.rename.tooltip"), () -> this);
    private final DataComponentListDialog toggleableComponentsDialog = new DataComponentListDialog(Bukkit.getServer().getPluginManager().getPermission("shia.rename.component"), () -> this, (type, item, buttonFactory) -> !(buttonFactory instanceof AbstractBasicDialogFactory), 1);
    private final DataComponentListDialog addComponentsDialog = new DataComponentListDialog(Bukkit.getServer().getPluginManager().getPermission("shia.rename.component"), () -> this, (type, item, buttonFactory) -> !item.hasData(type));
    private final DataComponentListDialog editComponentsDialog = new DataComponentListDialog(Bukkit.getServer().getPluginManager().getPermission("shia.rename.component"), () -> this, (type, item, buttonFactory) -> item.hasData(type));

    public MainRenameDialog(Permission permission) {
        super(null, null);
    }

    public Stream<AbstractExternalButtonFactory> dialogFactories() {
        return Stream.of(
            itemNameRenameDialog,
            itemCustomNameRenameDialog,
            itemLoreRenameDialog,
            tooltipDisplayDialog,
            toggleableComponentsDialog,
            addComponentsDialog,
            editComponentsDialog
        );
    }

    @Override
    protected List<ActionButton> getDialogButtons(Player player, ItemStack item) {
        return dialogFactories()
            .filter(factory -> factory.hasPermission(player))
            .map(factory -> factory.openActionButton(player))
            .toList();
    }

    public void reload(YamlFileConfig configFile, ConfigurationSection configSection) {
        super.reload(configFile, configSection);
        itemNameRenameDialog.reload(configFile, configFile.getConfigurationSection("dialog.item-name"));
        itemCustomNameRenameDialog.reload(configFile, configFile.getConfigurationSection("dialog.custom-name"));
        itemLoreRenameDialog.reload(configFile, configFile.getConfigurationSection("dialog.lore"));
        tooltipDisplayDialog.reload(configFile, configFile.getConfigurationSection("dialog.tooltip"));
        toggleableComponentsDialog.reload(configFile, configFile.getConfigurationSection("dialog.toggles"));
        addComponentsDialog.reload(configFile, configFile.getConfigurationSection("dialog.add-components"));
        editComponentsDialog.reload(configFile, configFile.getConfigurationSection("dialog.edit-components"));
    }


}
