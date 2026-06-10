package at.iamsoccer.soccerisawesome.itemrename.dialog.component;

import at.iamsoccer.soccerisawesome.itemrename.ItemRenameModule;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractButtonListDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.AbstractDialogFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.DialogButton;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class ResetRemoveDataComponentEditorDialog extends AbstractButtonListDialog {
    private final DataComponentType dataComponentType;

    private final DialogButton<Player> resetComponentButton;
    private final DialogButton<Player> removeComponentButton;

    public ResetRemoveDataComponentEditorDialog(
        @Nullable Supplier<AbstractDialogFactory<Player>> returnDialogFactorySupplier,
        DataComponentType dataComponentType
    ) {
        super(ItemRenameModule.createPermission(dataComponentType), returnDialogFactorySupplier);
        this.dataComponentType = dataComponentType;

        this.resetComponentButton = newButton("reset-component", (response, player) -> {
            if (!tryOpen(player)) return;
            player.getInventory().getItemInMainHand().resetData(dataComponentType);
            returnToPrevious(player);
        });
        this.removeComponentButton = newButton("remove-component", (response, player) -> {
            if (!tryOpen(player)) return;
            var item = player.getInventory().getItemInMainHand();
            if (item.getType().asItemType().hasDefaultData(dataComponentType)) item.unsetData(dataComponentType);
            else item.resetData(dataComponentType);
            returnToPrevious(player);
        });
    }

    @Override
    public TagResolver tagResolver(Player player, @Nullable DialogResponseView response) {
        return TagResolver.builder()
            .resolver(super.tagResolver(player, response))
            .tag("component", Tag.preProcessParsed(dataComponentType.key().asMinimalString()))
            .build();
    }

    @Override
    protected List<ActionButton> getDialogButtons(Player player) {
        var item = player.getInventory().getItemInMainHand();
        var actions = new ArrayList<ActionButton>(2);
        if (item.hasData(dataComponentType) && !item.getType().asItemType().hasDefaultData(dataComponentType)
            || !item.hasData(dataComponentType) && item.getType().asItemType().hasDefaultData(dataComponentType)
            || dataComponentType instanceof DataComponentType.Valued<?> valued && !item.getData(valued).equals(item.getType().asItemType().getDefaultData(valued))) {
            actions.add(resetComponentButton.button(player));
        }
        if (item.hasData(dataComponentType)) {
            actions.add(removeComponentButton.button(player));
        }
        return actions;
    }
}
