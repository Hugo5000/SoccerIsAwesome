package at.iamsoccer.soccerisawesome.itemrename.dialog.templates;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.dialog.DialogLike;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractButtonListDialog extends AbstractBasicDialogFactory {
    public AbstractButtonListDialog(Permission permission, @Nullable Supplier<IDialogFactory> returnFactorySupplier) {
        super(permission, returnFactorySupplier);
    }

    @Override
    public DialogLike create(Player player) {
        var item = player.getInventory().getItemInMainHand().clone();

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
