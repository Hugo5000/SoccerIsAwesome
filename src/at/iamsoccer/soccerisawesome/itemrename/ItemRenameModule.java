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
import io.papermc.paper.datacomponent.DataComponentType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemRenameModule extends AbstractModule implements Listener {
    public final ItemCustomNameRenameDialog itemCustomNameRenameDialog = new ItemCustomNameRenameDialog(plugin.getServer().getPluginManager().getPermission("shia.rename.custom-name.command"), null);
    public final ItemNameRenameDialog itemNameRenameDialog = new ItemNameRenameDialog(plugin.getServer().getPluginManager().getPermission("shia.rename.item-name"), null);
    public final ItemLoreRenameDialog itemLoreRenameDialog = new ItemLoreRenameDialog(plugin.getServer().getPluginManager().getPermission("shia.rename.lore"), null);

    public final MainRenameDialog mainRenameDialog = new MainRenameDialog(plugin.getServer().getPluginManager().getPermission("shia.rename.command"));

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
        mainRenameDialog.reload(config, config.getConfigurationSection("dialog.main"));
    }

    @Override
    public void lifecycleHandler(SoccerIsAwesomePlugin.ICommandRegistration register) {
        register.register(Commands.literal("rename")
                .requires(css -> css.getSender() instanceof Player player && itemCustomNameRenameDialog.hasPermission(player))
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
                .requires(css -> css.getSender() instanceof Player player && itemLoreRenameDialog.hasPermission(player))
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
                .requires(css -> css.getSender() instanceof Player player && itemNameRenameDialog.hasPermission(player))
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



    public static Permission createPermission(@SuppressWarnings("UnstableApiUsage") DataComponentType dataComponentType) {
        final String permissionName = "shia.rename.component." + dataComponentType.key().asString().replace(":", ".");
        @Nullable var perm = Bukkit.getPluginManager().getPermission(permissionName);
        if (perm != null) return perm;
        perm = new Permission(permissionName, "Allows you to add a %s component".formatted(dataComponentType.key().asMinimalString()), PermissionDefault.OP);
        Bukkit.getServer().getPluginManager().addPermission(perm);
        return perm;
    }
}
