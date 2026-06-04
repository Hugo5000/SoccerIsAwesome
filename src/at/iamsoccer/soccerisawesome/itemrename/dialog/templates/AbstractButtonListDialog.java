package at.iamsoccer.soccerisawesome.itemrename.dialog.templates;

import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.dialog.DialogLike;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractButtonListDialog extends AbstractItemDialogFactory {

    public AbstractButtonListDialog(@Nullable Permission permission, @Nullable Supplier<AbstractDialogFactory<Player>> returnFactorySupplier) {
        super(permission, returnFactorySupplier);
    }

    @Override
    protected void open(@Nullable DialogResponseView response, Player player, ItemStack item) {
        player.showDialog(create(player, item));
    }

    private DialogLike create(Player player, ItemStack item) {
        return createDialog(body -> {
                body.add(DialogBody.item(item).build());
                return body;
            }, Collections.emptyList(), closeButton ->
                DialogType.multiAction(getDialogButtons(player, item))
                    .columns(getColumns())
                    .exitAction(closeButton.button(player))
                    .build()
        );
    }

    @Positive
    protected int getColumns() {
        return 1;
    }

    protected abstract List<ActionButton> getDialogButtons(Player player, ItemStack item);
}
