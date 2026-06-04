package at.iamsoccer.soccerisawesome.itemrename.dialog.templates.interfaces;

import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;

public interface IPlayerOpenable {
    default void onOpen(Audience audience) {
        if (audience instanceof Player player) onOpen(player);
    }
    void onOpen(Player player);
}
