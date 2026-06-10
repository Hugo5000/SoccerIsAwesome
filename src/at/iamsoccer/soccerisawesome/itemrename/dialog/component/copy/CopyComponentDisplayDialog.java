package at.iamsoccer.soccerisawesome.itemrename.dialog.component.copy;

import at.hugob.plugin.library.config.ConfigUtils;
import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractButtonListDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.AbstractDialogFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.DialogButton;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.registry.data.dialog.ActionButton;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class CopyComponentDisplayDialog extends AbstractButtonListDialog {
    private DialogButton.ButtonInfo noOffHandItemInfo = new DialogButton.ButtonInfo(Component.empty(), null);

    public CopyComponentDisplayDialog(@Nullable Permission permission, @Nullable Supplier<AbstractDialogFactory<Player>> returnDialogFactorySupplier) {
        super(permission, returnDialogFactorySupplier);
    }

    private final DialogButton<Player> copyAllButton = newButton("copy-all", (response, player) -> {
        if (!tryOpen(player)) return;
        var mainItem = player.getInventory().getItemInMainHand();
        var offItem = player.getInventory().getItemInOffHand();
        mainItem.copyDataFrom(offItem, comp -> true);
        open(player);
    });
    private final DialogButton<Player> makeCopyButton = newButton("make-copy", (response, player) -> {
        if (!tryOpen(player)) return;
        var mainItem = player.getInventory().getItemInMainHand();
        var offItem = player.getInventory().getItemInOffHand();
        for (DataComponentType mainComponent : getComponents(mainItem)) {
            if (mainItem.getType().asItemType().hasDefaultData(mainComponent)) {
                mainItem.unsetData(mainComponent);
            } else {
                mainItem.resetData(mainComponent);
            }
        }
        mainItem.copyDataFrom(offItem, comp -> true);
        open(player);
    });
    private final CopyComponentsDisplayDialog copyComponentsDisplayDialog = new CopyComponentsDisplayDialog(permission, () -> this);

    @Override
    public void reload(YamlFileConfig configFile, ConfigurationSection configSection) {
        super.reload(configFile, configSection);
        copyComponentsDisplayDialog.reload(configFile, configSection.getConfigurationSection("copy-specific"));
        noOffHandItemInfo = new DialogButton.ButtonInfo(
            ConfigUtils.parseComponent(configFile, configSection.getString("no-off-hand-item.label"), null, null),
            configSection.isSet("no-off-hand-item.tooltip") ? ConfigUtils.parseComponent(configFile, configSection.getString("no-off-hand-item.tooltip"), null, null) : null
        );
    }

    @Override
    protected List<ActionButton> getDialogButtons(Player player) {
        return List.of(
            copyComponentsDisplayDialog.externalButton().button(player),
            copyAllButton.button(player),
            makeCopyButton.button(player)
        );
    }

    private Set<DataComponentType> getComponents(ItemStack itemStack) {
        var set = new HashSet<DataComponentType>();
        set.addAll(itemStack.getDataTypes());
        set.addAll(itemStack.getType().asItemType().getDefaultDataTypes());
        return set;
    }

    @Override
    protected boolean tryOpen(Player player) {
        if (!super.tryOpen(player)) return false;
        if (player.getInventory().getItemInOffHand().isEmpty()) {
            returnToPrevious(player);
            return false;
        }
        return true;
    }

    @Override
    protected DialogButton.IButtonInfoSupplier<Player> buttonInfoSupplier(String name) {
        var sup =  super.buttonInfoSupplier(name);
        if("external".equals(name)) {
            return player -> {
                if (player.getInventory().getItemInOffHand().isEmpty()) {
                    return noOffHandItemInfo;
                }
               return sup.supplyFor(player);
            };
        }
        return sup;
    }
}
