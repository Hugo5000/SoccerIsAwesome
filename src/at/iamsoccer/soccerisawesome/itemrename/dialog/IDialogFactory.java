package at.iamsoccer.soccerisawesome.itemrename.dialog;

import net.kyori.adventure.dialog.DialogLike;
import org.bukkit.entity.Player;

public interface IDialogFactory {
    DialogLike create(Player player, boolean returnToMain);
    boolean hasPermission(Player player);
}
