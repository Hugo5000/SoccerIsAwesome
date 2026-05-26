package at.iamsoccer.soccerisawesome.itemrename;

import at.iamsoccer.soccerisawesome.AbstractModule;
import at.iamsoccer.soccerisawesome.SoccerIsAwesomePlugin;
import at.iamsoccer.soccerisawesome.itemrename.dialog.ItemRenameDialog;
import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;

import java.util.List;

public class ItemRenameModule extends AbstractModule {
    public ItemRenameModule(SoccerIsAwesomePlugin plugin) {
        super(plugin, "ItemRename");
    }

    @Override
    public void lifecycleHandler(SoccerIsAwesomePlugin.ICommandRegistration register) {
        register.register(Commands.literal("rename")
                .requires(css -> css.getSender() instanceof Player)
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    player.showDialog(ItemRenameDialog.create(player));
                    return Command.SINGLE_SUCCESS;
                })
                .build(),
            "Allows renaming of an Items Name",
            List.of("setname")
        );
        register.register(Commands.literal("relore")

                .build(),
            "Allows changing an Items Lore",
            List.of("setlore")
        );
        register.register(Commands.literal("relore")

                .build(),
            "Allows renaming of an Items Name",
            List.of("setlore")
        );
    }
}
