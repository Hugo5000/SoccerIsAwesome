package at.iamsoccer.soccerisawesome.itemrename.dialog.templates.interfaces;

import io.papermc.paper.registry.data.dialog.ActionButton;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Range;

@SuppressWarnings("UnstableApiUsage")
public interface IActionButtonFactory {
    ActionButton openActionButton(Player player);
    ActionButton openActionButton(Player player, @Range(from = 1, to = 1024) int width);
}
