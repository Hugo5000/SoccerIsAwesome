package at.iamsoccer.soccerisawesome.itemrename.dialog.templates;

import net.kyori.adventure.dialog.DialogLike;
import org.bukkit.entity.Player;

public interface IDialogFactory {
    DialogLike create(Player player);
    boolean hasPermission(Player player);
}
