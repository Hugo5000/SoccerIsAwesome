package at.iamsoccer.soccerisawesome.itemrename.dialog;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
    protected SuggestionResult getSuggestionFromItem(Player player, ItemStack item) {
        final String suggestion;
        final String plain;
        if (item.getPersistentDataContainer().has(pdcDataKey, PersistentDataType.TAG_CONTAINER)) {
            var container = item.getPersistentDataContainer().get(pdcDataKey, PersistentDataType.TAG_CONTAINER);
            suggestion = container.get(rawDataKey, PersistentDataType.STRING);
            plain = container.get(plainDataKey, PersistentDataType.STRING);
        } else if (item.hasData(DataComponentTypes.ITEM_NAME)) {
            suggestion = parseComponent(item.getData(DataComponentTypes.ITEM_NAME));
            plain = PlainTextComponentSerializer.plainText().serialize(item.getData(DataComponentTypes.ITEM_NAME));
        } else {
            suggestion = parseComponent(item.effectiveName());
            plain = PlainTextComponentSerializer.plainText().serialize(item.effectiveName());
        }
        var deserialized = PlainTextComponentSerializer.plainText().serialize(parseLine(player, suggestion));
        return new SuggestionResult(suggestion, !deserialized.equals(plain));
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
    protected void applyToPDC(Player player, PersistentDataContainer pdc, String input) {
        if (input.isBlank()) {
            pdc.remove(pdcDataKey);
        } else {
            var container = pdc.getAdapterContext().newPersistentDataContainer();
            container.set(rawDataKey, PersistentDataType.STRING, input);
            container.set(plainDataKey, PersistentDataType.STRING,
                PlainTextComponentSerializer.plainText().serialize(parseLine(player, input)));
            pdc.set(pdcDataKey, PersistentDataType.TAG_CONTAINER, container);
        }
    }
}
