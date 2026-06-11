package at.iamsoccer.soccerisawesome.itemrename.dialog.rename;

import at.iamsoccer.soccerisawesome.itemrename.ItemRenameModule;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.AbstractDialogFactory;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
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

    public ItemCustomNameRenameDialog(@Nullable Permission permission, @Nullable Supplier<AbstractDialogFactory<Player>> returnDialogSupplier) {
        super(permission, returnDialogSupplier);
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
        setInItem(player, input, item);
    }

    public static void setInItem(Player player, String input, ItemStack item) {
        if (input.isBlank()) {
            item.resetData(DataComponentTypes.CUSTOM_NAME);
        } else {
            item.setData(DataComponentTypes.CUSTOM_NAME, SignedComponent.sign(player, input, serializerFor(player)));
        }
    }

    public static SuggestionResult getCustomNameSuggestionFromItem(Player player, ItemStack item) {
        SignedComponent res;
        if (item.hasData(DataComponentTypes.CUSTOM_NAME)) {
            res = SignedComponent.parse(item.getData(DataComponentTypes.CUSTOM_NAME));
        } else if (item.hasData(DataComponentTypes.ITEM_NAME)) {
            res = SignedComponent.parse(item.getData(DataComponentTypes.ITEM_NAME));
        } else {
            res = SignedComponent.parse(item.effectiveName());
        }
        var deserialized = PlainTextComponentSerializer.plainText().serialize(SignedComponent.sign(player, res.rawText(), serializerFor(player))).hashCode();
        return new SuggestionResult(res.rawText(), deserialized != res.plainHash());
    }
}
