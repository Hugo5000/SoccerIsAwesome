package at.iamsoccer.soccerisawesome.itemrename.dialog.templates;

import net.kyori.adventure.dialog.DialogLike;
import org.bukkit.entity.Player;

public interface IDialogFactory extends IPermissible {
    DialogLike create(Player player);
}
