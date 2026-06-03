package at.iamsoccer.soccerisawesome.itemrename.dialog.component;

import at.iamsoccer.soccerisawesome.itemrename.ItemRenameModule;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractExternalButtonFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.IDialogFactory;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class ToggleDataComponentEditorDialog extends AbstractExternalButtonFactory {
    private final DataComponentType.NonValued dataComponentType;

    public ToggleDataComponentEditorDialog(
        @Nullable Supplier<IDialogFactory> returnDialogFactorySupplier,
        DataComponentType.NonValued dataComponentType
    ) {
        super(ItemRenameModule.createPermission(dataComponentType), returnDialogFactorySupplier);
        this.dataComponentType = dataComponentType;
    }

    @Override
    protected void onClick(Player player) {
        var item = player.getInventory().getItemInMainHand();
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
            title,
            Component.text("This has not been fully implemented, you can only remove or reset it to default.")
        );
    }
}
