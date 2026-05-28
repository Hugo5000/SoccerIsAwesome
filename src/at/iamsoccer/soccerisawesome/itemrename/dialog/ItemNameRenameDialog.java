package at.iamsoccer.soccerisawesome.itemrename.dialog;

import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ItemNameRenameDialog extends AbstractRenameDialog {
    public ItemNameRenameDialog() {
        super(NamespacedKey.fromString("rename:item_name"));
    }

    @Override
    protected String getSuggestionFromItem(ItemStack item) {
        if (item.getPersistentDataContainer().has(pdcDataKey, PersistentDataType.STRING)) {
            return item.getPersistentDataContainer().get(pdcDataKey, PersistentDataType.STRING);
        } else if (item.hasData(DataComponentTypes.ITEM_NAME)) {
            return parseComponent(item.getData(DataComponentTypes.ITEM_NAME));
        } else {
            return parseComponent(item.effectiveName());
        }
    }

    @Override
    protected void applyToItem(Player player, String input, ItemStack item) {
        if (input.isBlank()) {
            item.resetData(DataComponentTypes.ITEM_NAME);
        } else {
            item.setData(DataComponentTypes.ITEM_NAME, parseLine(player, input));
        }
    }

    @Override
    protected void applyToPDC(PersistentDataContainer pdc, String input) {
        if (input.isBlank()) {
            pdc.remove(pdcDataKey);
        } else {
            pdc.set(pdcDataKey, PersistentDataType.STRING, input);
        }
    }
}
