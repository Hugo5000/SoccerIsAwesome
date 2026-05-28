package at.iamsoccer.soccerisawesome.itemrename;

import at.iamsoccer.soccerisawesome.AbstractModule;
import at.iamsoccer.soccerisawesome.SoccerIsAwesomePlugin;
import at.iamsoccer.soccerisawesome.itemrename.dialog.ItemCustomNameRenameDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.ItemLoreRenameDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.ItemNameRenameDialog;
import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.List;

public class ItemRenameModule extends AbstractModule implements Listener {
    private final ItemCustomNameRenameDialog itemCustomNameRenameDialog = new ItemCustomNameRenameDialog();
    private final ItemNameRenameDialog itemNameRenameDialog = new ItemNameRenameDialog();
    private final ItemLoreRenameDialog itemLoreRenameDialog = new ItemLoreRenameDialog();

    public ItemRenameModule(SoccerIsAwesomePlugin plugin) {
        super(plugin, "ItemRename");
    }

    @Override
    public void lifecycleHandler(SoccerIsAwesomePlugin.ICommandRegistration register) {
        register.register(Commands.literal("rename")
                .requires(css -> css.getSender() instanceof Player player && player.hasPermission("shia.rename.custom-name.command"))
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    player.showDialog(itemCustomNameRenameDialog.create(player));
                    return Command.SINGLE_SUCCESS;
                })
                .build(),
            "Allows renaming of an Items Name",
            List.of("setname")
        );
        register.register(Commands.literal("relore")
                .requires(css -> css.getSender() instanceof Player player && player.hasPermission("shia.rename.lore"))
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
                .requires(css -> css.getSender() instanceof Player player && player.hasPermission("shia.rename.item-name"))
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    player.showDialog(itemLoreRenameDialog.create(player));
                    return Command.SINGLE_SUCCESS;
                })
                .build(),
            "Allows changing an Items Lore",
            List.of("setitemname")
        );
    }
}
