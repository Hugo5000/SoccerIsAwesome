package at.iamsoccer.soccerisawesome.itemrename.dialog.templates;

import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.AbstractDialogFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.DialogButton;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractButtonListDialog extends AbstractItemDialogFactory {
    public AbstractButtonListDialog(@Nullable Permission permission, @Nullable Supplier<AbstractDialogFactory<Player>> returnFactorySupplier) {
        super(permission, returnFactorySupplier);
    }

    @Override
    protected DialogType dialogType(DialogButton<Player> closeButton, Player player, @Nullable DialogResponseView response) {
        return DialogType.multiAction(getDialogButtons(player))
            .exitAction(closeButton.button(player))
            .columns(getColumns())
            .build();
    }

    @Positive
    protected int getColumns() {
        return 1;
    }

    protected abstract List<ActionButton> getDialogButtons(Player player);
}
