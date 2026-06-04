package at.iamsoccer.soccerisawesome.itemrename.dialog.templates.interfaces;

import org.bukkit.entity.Player;

public interface IPermissibleOpenable extends IPlayerOpenable, IPermissible {
    @Override
    default void onOpen(Player player) {
        if (!hasPermission(player)) return;
        onValidatedOpen(player);
    }
    void onValidatedOpen(Player player);
}
