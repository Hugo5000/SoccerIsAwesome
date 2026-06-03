package at.iamsoccer.soccerisawesome.itemrename.dialog.templates;

import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface IActionButtonFactory {
    ActionButton openActionButton(Player player);
    @Nullable DialogAction openAction();
}
