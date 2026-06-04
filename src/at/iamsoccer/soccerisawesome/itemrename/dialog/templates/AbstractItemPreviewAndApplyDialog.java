package at.iamsoccer.soccerisawesome.itemrename.dialog.templates;

import at.hugob.plugin.library.config.YamlFileConfig;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractItemPreviewAndApplyDialog extends AbstractPreviewAndApplyEditorDialog<Player> {
    private final AbstractDialogFactory<Player> missingItemDialog = new AbstractDialogFactory<>(Player.class, null) {
        @Override
        protected boolean isAllowedToOpenInternal(Player player) {
            return true;
        }

        @Override
        protected void open(@Nullable DialogResponseView response, Player player) {
            player.showDialog(createDialog(Component.text("Not holding an Item!", NamedTextColor.RED), null, List.of(), playerDialogButton -> DialogType.notice(playerDialogButton.button(player))));
        }
    };
    private final @Nullable Permission permission;

    public AbstractItemPreviewAndApplyDialog(@Nullable Permission permission, @Nullable Supplier<AbstractDialogFactory<Player>> returnDialogFactorySupplier) {
        super(Player.class, returnDialogFactorySupplier);
        this.permission = permission;
    }

    @Override
    public void reload(YamlFileConfig configFile, ConfigurationSection configSection) {
        super.reload(configFile, configSection);
        missingItemDialog.reload(configFile, configSection);
    }

    @Override
    protected final List<DialogBody> body(List<DialogBody> body, Player player, @Nullable DialogResponseView responseView) {
        var item = player.getInventory().getItemInMainHand();
        return body(body, player, responseView, item);
    }
    protected abstract List<DialogBody> body(List<DialogBody> body, Player player, @Nullable DialogResponseView responseView, ItemStack item);

    @Override
    protected final List<DialogInput> inputs(Player player, @Nullable DialogResponseView responseView) {
        var item = player.getInventory().getItemInMainHand();
        return inputs(player, responseView, item);
    }
    protected abstract List<DialogInput> inputs(Player player, @Nullable DialogResponseView responseView, ItemStack item);

    @Override
    protected final void onApply(DialogResponseView response, Player player) {
        var item = player.getInventory().getItemInMainHand();
        onApply(response, player, item);
    }
    protected abstract void onApply(DialogResponseView response, Player player, ItemStack item);

    @Override
    protected final boolean isAllowedToOpenInternal(Player player) {
        return permission == null || player.hasPermission(permission);
    }

    @Override
    protected boolean tryToOpenInternal(Player player) {
        if (permission != null && !player.hasPermission(permission)) return false;
        boolean hasItem = !player.getInventory().getItemInMainHand().isEmpty();
        if (!hasItem) {
            missingItemDialog.open(player);
        }
        return hasItem;
    }
}
