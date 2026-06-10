package at.iamsoccer.soccerisawesome.itemrename.dialog.templates;

import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.AbstractConfigDialogButtonFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.AbstractDialogFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.ConfigDialogFactory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public abstract class AbstractItemDialogButtonFactory extends AbstractConfigDialogButtonFactory<Player> {
    private final ConfigDialogFactory<Player> missingItemDialog;
    protected final @Nullable Permission permission;

    public AbstractItemDialogButtonFactory(@Nullable Permission permission, @Nullable Supplier<AbstractDialogFactory<Player>> returnFactorySupplier) {
        super(Player.class, returnFactorySupplier);
        this.missingItemDialog = new ConfigDialogFactory<>(Player.class, returnFactorySupplier);
        this.permission = permission;
    }

    @Override
    public void reload(YamlFileConfig configFile, ConfigurationSection configSection) {
        super.reload(configFile, configSection);
        missingItemDialog.reload(configFile, configSection);
    }

    @Override
    public boolean isAllowedToOpen(Player player) {
        if (!super.isAllowedToOpen(player)) return false;
        return permission == null || player.hasPermission(permission);
    }

    @Override
    protected boolean tryOpen(Player player) {
        if (!super.tryOpen(player)) return false;
        boolean hasItem = !player.getInventory().getItemInMainHand().isEmpty();
        if (!hasItem) {
            missingItemDialog.open(player);
        }
        return hasItem;
    }
}
