package at.iamsoccer.soccerisawesome.itemrename.dialog.rename;

import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.IDialogFactory;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class ItemCustomNameRenameDialog extends AbstractRenameDialog {

    public static final NamespacedKey CUSTOM_NAME_KEY = NamespacedKey.fromString("rename:custom_name");

    public ItemCustomNameRenameDialog(@Nullable Permission permission, @Nullable Supplier<IDialogFactory> returnDialogSupplier) {
        super(CUSTOM_NAME_KEY, permission, returnDialogSupplier);
    }

    @Override
    protected SuggestionResult getSuggestionFromItem(Player player, ItemStack item) {
        return getCustomNameSuggestionFromItem(player, item);
    }

    @Override
    protected boolean isDifferentThanExpected(Player player, ItemStack item) {
        return getSuggestionFromItem(player, item).isDifferent();
    }

    @Override
    protected void applyToItem(Player player, String input, ItemStack item) {
        setCustomNameInItem(player, input, item);
    }

    public static void setCustomNameInItem(Player player, String input, ItemStack item) {
        if (input.isBlank()) {
            item.resetData(DataComponentTypes.CUSTOM_NAME);
        } else {
            item.setData(DataComponentTypes.CUSTOM_NAME, parseLine(player, input));
        }
    }

    @Override
    protected void applyToPDC(Player player, PersistentDataContainer pdc, String input) {
        setCustomNameInPDC(player, pdc, input);
    }

    public static void setCustomNameInPDC(Player player, PersistentDataContainer pdc, String input) {
        if (input.isBlank()) {
            pdc.remove(CUSTOM_NAME_KEY);
        } else {
            var container = pdc.getAdapterContext().newPersistentDataContainer();
            container.set(rawDataKey, PersistentDataType.STRING, input);
            container.set(plainDataKey, PersistentDataType.STRING,
                PlainTextComponentSerializer.plainText().serialize(parseLine(player, input)));
            pdc.set(CUSTOM_NAME_KEY, PersistentDataType.TAG_CONTAINER, container);
        }
    }

    public static SuggestionResult getCustomNameSuggestionFromItem(Player player, ItemStack item) {
        final String suggestion;
        final String plain;
        if (item.getPersistentDataContainer().has(CUSTOM_NAME_KEY, PersistentDataType.TAG_CONTAINER)) {
            var container = item.getPersistentDataContainer().get(CUSTOM_NAME_KEY, PersistentDataType.TAG_CONTAINER);
            suggestion = container.get(rawDataKey, PersistentDataType.STRING);
            plain = container.get(plainDataKey, PersistentDataType.STRING);
        } else if (item.hasData(DataComponentTypes.CUSTOM_NAME)) {
            suggestion = parseComponent(item.getData(DataComponentTypes.CUSTOM_NAME));
            plain = PlainTextComponentSerializer.plainText().serialize(item.getData(DataComponentTypes.CUSTOM_NAME));
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
}
