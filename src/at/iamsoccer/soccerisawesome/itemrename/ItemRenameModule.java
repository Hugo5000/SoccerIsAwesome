package at.iamsoccer.soccerisawesome.itemrename;

import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.AbstractModule;
import at.iamsoccer.soccerisawesome.SoccerIsAwesomePlugin;
import at.iamsoccer.soccerisawesome.itemrename.dialog.MainRenameDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.rename.ItemCustomNameRenameDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.rename.ItemLoreRenameDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.rename.ItemNameRenameDialog;
import co.aikar.commands.PaperCommandManager;
import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.List;

public class ItemRenameModule extends AbstractModule implements Listener {
    private final ItemCustomNameRenameDialog itemCustomNameRenameDialog = new ItemCustomNameRenameDialog(plugin.getServer().getPluginManager().getPermission("shia.rename.custom-name.command"));
    private final ItemNameRenameDialog itemNameRenameDialog = new ItemNameRenameDialog(plugin.getServer().getPluginManager().getPermission("shia.rename.item-name"));
    private final ItemLoreRenameDialog itemLoreRenameDialog = new ItemLoreRenameDialog(plugin.getServer().getPluginManager().getPermission("shia.rename.lore"));

    private final MainRenameDialog mainRenameDialog = new MainRenameDialog(
        itemNameRenameDialog,
        itemCustomNameRenameDialog,
        itemLoreRenameDialog
    );

    private YamlFileConfig config;

    public ItemRenameModule(SoccerIsAwesomePlugin plugin) {
        super(plugin, "ItemRename");
    }

    @Override
    public boolean enable(PaperCommandManager commandManager) {
        if (!super.enable(commandManager)) return false;
        config = new YamlFileConfig(plugin, "rename-config.yml");
        return true;
    }

    @Override
    public void reload() {
        config.reload();
        itemCustomNameRenameDialog.reload(config, config.getConfigurationSection("dialog.custom-name"));
        itemNameRenameDialog.reload(config, config.getConfigurationSection("dialog.item-name"));
        itemLoreRenameDialog.reload(config, config.getConfigurationSection("dialog.lore"));
    }

    @Override
    public void lifecycleHandler(SoccerIsAwesomePlugin.ICommandRegistration register) {
        register.register(Commands.literal("rename")
                .requires(css -> css.getSender() instanceof Player player && player.hasPermission(itemCustomNameRenameDialog.permission))
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    player.showDialog(itemCustomNameRenameDialog.create(player));
                    return Command.SINGLE_SUCCESS;
                })
                .build(),
            "Allows renaming of an Items Name",
            List.of("setname", "setcustomname")
        );
        register.register(Commands.literal("relore")
                .requires(css -> css.getSender() instanceof Player player && player.hasPermission(itemLoreRenameDialog.permission))
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    player.showDialog(itemLoreRenameDialog.create(player));
                    return Command.SINGLE_SUCCESS;
                })
                .build(),
            "Allows changing an Items Lore",
            List.of("setlore")
        );
        register.register(Commands.literal("reitemname")
                .requires(css -> css.getSender() instanceof Player player && player.hasPermission(itemNameRenameDialog.permission))
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    player.showDialog(itemNameRenameDialog.create(player));
                    return Command.SINGLE_SUCCESS;
                })
                .build(),
            "Allows changing an Items Lore",
            List.of("setitemname")
        );
        register.register(Commands.literal("shiarename")
                .requires(css -> css.getSender() instanceof Player player && player.hasPermission("shia.rename.command"))
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    player.showDialog(mainRenameDialog.create(player));
                    return Command.SINGLE_SUCCESS;
                })
                .build(),
            "Allows using various admin commands for items",
            List.of("sr"));
    }
}
