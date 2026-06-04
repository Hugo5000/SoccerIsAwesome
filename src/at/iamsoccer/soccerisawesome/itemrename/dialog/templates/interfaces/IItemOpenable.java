package at.iamsoccer.soccerisawesome.itemrename.dialog.templates.interfaces;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface IItemOpenable extends IPermissibleOpenable {
    @Override
    default void onValidatedOpen(Player player) {
        var item = player.getInventory().getItemInMainHand();
        if (!validateItem(player, item)) return;
        onOpen(player, item);
    }
    void onOpen(Player player, ItemStack item);
    default boolean validateItem(Player player, ItemStack item) {
        return !item.isEmpty();
    }
}
