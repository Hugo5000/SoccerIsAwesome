package at.iamsoccer.soccerisawesome.itemrename.dialog.templates.interfaces;

import net.kyori.adventure.dialog.DialogLike;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface IItemDialogFactory extends IPermissibleOpenable, IItemOpenable {
    @Override
    default void onOpen(Player player) {
        IItemOpenable.super.onOpen(player);
    }
    @Override
    void onOpen(Player player, ItemStack item);
}
