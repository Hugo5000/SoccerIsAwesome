package at.iamsoccer.soccerisawesome.itemrename.dialog.templates;

import at.hugob.plugin.library.config.YamlFileConfig;
import io.papermc.paper.dialog.DialogResponseView;
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

@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractItemDialogFactory extends AbstractDialogFactory<Player> {
    private final AbstractDialogFactory<Player> missingItemDialog = new AbstractDialogFactory<>(Player.class, null) {
        @Override
        protected boolean isAllowedToOpenInternal(Player player) {
            return true;
        }

        @Override
        protected void open(@Nullable DialogResponseView response, Player player) {
            player.showDialog(createDialog(Component.text("You need to be holding an item in your main hand!", NamedTextColor.RED), null, List.of(), playerDialogButton -> DialogType.notice(playerDialogButton.button(player))));
        }
    };
    protected final @Nullable Permission permission;

    public AbstractItemDialogFactory(@Nullable Permission permission, @Nullable Supplier<AbstractDialogFactory<Player>> returnFactorySupplier) {
        super(Player.class, returnFactorySupplier);
        this.permission = permission;
    }

    @Override
    public void reload(YamlFileConfig configFile, ConfigurationSection configSection) {
        super.reload(configFile, configSection);
        missingItemDialog.reload(configFile, configSection);
    }

    @Override
    protected final void open(@Nullable DialogResponseView response, Player player) {
        var item = player.getInventory().getItemInMainHand();
        open(response, player, item);
    }

    protected abstract void open(@Nullable DialogResponseView response, Player user, ItemStack item);

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
