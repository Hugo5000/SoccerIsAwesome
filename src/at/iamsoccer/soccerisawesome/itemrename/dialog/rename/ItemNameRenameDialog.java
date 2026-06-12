package at.iamsoccer.soccerisawesome.itemrename.dialog.rename;

import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.AbstractDialogFactory;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.dialog.DialogResponseView;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class ItemNameRenameDialog extends AbstractRenameDialog {
    public ItemNameRenameDialog(@Nullable Permission permission, @Nullable Supplier<AbstractDialogFactory<Player>> returnDialogSupplier) {
        super(permission, returnDialogSupplier);
    }

    @Override
    protected SuggestionResult getSuggestionFromItem(Player player, ItemStack item) {
        SignedComponent res;
        if (item.hasData(DataComponentTypes.ITEM_NAME)) {
            res = SignedComponent.parse(item.getData(DataComponentTypes.ITEM_NAME));
        } else {
            res = SignedComponent.parse(item.effectiveName());
        }
        var deserialized = SignedComponent.sign(player.getUniqueId(), res.rawText(), serializerFor(player)).plainHash();
        return new SuggestionResult(res.rawText(), deserialized != res.plainHash());
    }

    @Override
    protected boolean isDifferentThanExpected(Player player, ItemStack item) {
        return getSuggestionFromItem(player, item).isDifferent();
    }

    @Override
    protected void modifyPreview(Player player, @Nullable DialogResponseView response, ItemStack item) {
        super.modifyPreview(player, response, item);
        item.unsetData(DataComponentTypes.CUSTOM_NAME);
    }

    @Override
    protected void applyToItem(Player player, String input, ItemStack item) {
        setInItem(player, input, item);
    }

    public static void setInItem(Player player, String input, ItemStack item) {
        if (input.isBlank()) {
            item.resetData(DataComponentTypes.ITEM_NAME);
        } else {
            item.setData(DataComponentTypes.ITEM_NAME, SignedComponent.sign(player.getUniqueId(), input, serializerFor(player)).component());
        }
    }
}
