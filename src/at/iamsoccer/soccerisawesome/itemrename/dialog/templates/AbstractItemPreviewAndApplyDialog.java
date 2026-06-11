package at.iamsoccer.soccerisawesome.itemrename.dialog.templates;

import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.AbstractDialogFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.DialogButton;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractItemPreviewAndApplyDialog extends AbstractButtonListDialog {
    private final DialogButton<Player> applyButton = newButton("apply", (response, user) -> {
        if (!tryOpen(user)) return;
        applyToItem(user, response, user.getInventory().getItemInMainHand());
        returnToPrevious(user);
    });
    private final DialogButton<Player> previewButton = newButton("preview", (response, user) -> {
        if (!tryOpen(user)) return;
        onPreview(user, response);
        open(user, response);
    });

    public AbstractItemPreviewAndApplyDialog(@Nullable Permission permission, @Nullable Supplier<AbstractDialogFactory<Player>> returnDialogFactorySupplier) {
        super(permission, returnDialogFactorySupplier);
    }

    protected void onPreview(Player player, DialogResponseView response) {
        open(player, response);
    }

    @Override
    protected List<ActionButton> getDialogButtons(Player player) {
        return new ArrayList<>(dialogButtons(player));
    }

    protected @NonNull List<ActionButton> dialogButtons(Player player) {
        return new ArrayList<>(List.of(
            previewButton.button(player),
            applyButton.button(player)
        ));
    }

    protected abstract void applyToItem(Player player, DialogResponseView response, ItemStack item);

    @Override
    protected void modifyPreview(Player player, @Nullable DialogResponseView response, ItemStack item) {
        if (response == null) return;
        applyToItem(player, response, item);
    }
}
