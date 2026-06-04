package at.iamsoccer.soccerisawesome.itemrename.dialog.component.primitives;

import at.iamsoccer.soccerisawesome.itemrename.ItemRenameModule;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractDialogFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractItemDialogButtonFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.buttons.DialogButton;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.dialog.DialogResponseView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class BooleanComponentDialog extends AbstractItemDialogButtonFactory {
    private final DataComponentType.Valued<Boolean> dataComponentType;
    private final Function<ItemStack, Boolean> defaultSupplier;

    public BooleanComponentDialog(
        @Nullable Supplier<AbstractDialogFactory<Player>> returnDialogFactorySupplier,
        DataComponentType.Valued<Boolean> dataComponentType,
        Function<ItemStack, Boolean> defaultSupplier
    ) {
        super(ItemRenameModule.createPermission(dataComponentType), returnDialogFactorySupplier);
        this.dataComponentType = dataComponentType;
        this.defaultSupplier = defaultSupplier;
    }

    @Override
    protected void onExternalButtonPressed(@Nullable DialogResponseView response, Player player, ItemStack item) {
        @Nullable var value = item.getData(dataComponentType);
        if (value == null) value = defaultSupplier.apply(item);
        if (value) {
            @Nullable var defaultValue = item.getType().asItemType().getDefaultData(dataComponentType);
            if (defaultValue == null) defaultValue = defaultSupplier.apply(item);
            if (!defaultValue) item.unsetData(dataComponentType);
            else item.setData(dataComponentType, false);
        } else {
            @Nullable var defaultValue = item.getType().asItemType().getDefaultData(dataComponentType);
            if (defaultValue == null) defaultValue = defaultSupplier.apply(item);
            if (defaultValue) item.unsetData(dataComponentType);
            else item.setData(dataComponentType, true);
        }
        returnToPrevious(player);
    }

    @Override
    protected DialogButton.ButtonInfo openButtonInfo(Player player) {
        var item = player.getInventory().getItemInMainHand();
        final Component title;
        var active = !item.hasData(dataComponentType) && defaultSupplier.apply(item)
                     || item.hasData(dataComponentType) && item.getData(dataComponentType);
        if (active) title = Component.text("Remove " + dataComponentType.key().asMinimalString(), NamedTextColor.RED);
        else title = Component.text("Add " + dataComponentType.key().asMinimalString(), NamedTextColor.GREEN);
        return new DialogButton.ButtonInfo(
            title, null
        );
    }

    public Map.Entry<DataComponentType.Valued<?>, BooleanComponentDialog> entry() {
        return Map.entry(dataComponentType, this);
    }
}
