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

import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class NonValuedComponentEditorDialog extends AbstractItemDialogButtonFactory {
    private final DataComponentType.NonValued dataComponentType;

    public NonValuedComponentEditorDialog(
        @Nullable Supplier<AbstractDialogFactory<Player>> returnDialogFactorySupplier,
        DataComponentType.NonValued dataComponentType
    ) {
        super(ItemRenameModule.createPermission(dataComponentType), returnDialogFactorySupplier);
        this.dataComponentType = dataComponentType;
    }

    @Override
    protected void onExternalButtonPressed(@Nullable DialogResponseView response, Player player, ItemStack item) {
        if (item.hasData(dataComponentType)) item.unsetData(dataComponentType);
        else item.setData(dataComponentType);
        returnToPrevious(player);
    }

    @Override
    protected DialogButton.ButtonInfo openButtonInfo(Player player) {
        var item = player.getInventory().getItemInMainHand();
        final Component title;
        if (item.hasData(dataComponentType))
            title = Component.text("Remove " + dataComponentType.key().asMinimalString(), NamedTextColor.RED);
        else title = Component.text("Add " + dataComponentType.key().asMinimalString(), NamedTextColor.GREEN);
        return new DialogButton.ButtonInfo(
            title, null
        );
    }
}
