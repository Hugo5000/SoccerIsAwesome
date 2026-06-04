package at.iamsoccer.soccerisawesome.itemrename.dialog.component.copy;

import at.hugob.plugin.library.config.ConfigUtils;
import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractButtonListDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractDialogFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.buttons.DialogButton;
import at.iamsoccer.soccerisawesome.itemrename.dialog.tooltip.SpecificTooltipDisplayDialog;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static at.iamsoccer.soccerisawesome.itemrename.dialog.rename.AbstractRenameDialog.UNLIMITED_CALLBACK_OPTIONS;

@SuppressWarnings("UnstableApiUsage")
public class CopyComponentDisplayDialog extends AbstractButtonListDialog {
    private DialogButton.ButtonInfo noOffHandItemInfo = new DialogButton.ButtonInfo(Component.empty(), null);

    public CopyComponentDisplayDialog(Permission permission, @Nullable Supplier<AbstractDialogFactory<Player>> returnDialogFactorySupplier) {
        super(permission, returnDialogFactorySupplier);
    }

    private final DialogButton<Player> copyAllButton = newButton("copy-all", null, (response, player) -> {
        if (!tryToOpenInternal(player)) return;
        var mainItem = player.getInventory().getItemInMainHand();
        var offItem = player.getInventory().getItemInOffHand();
        mainItem.copyDataFrom(offItem, comp -> true);
        open(player);
    });
    private final DialogButton<Player> makeCopyButton = newButton("make-copy", null, (response, player) -> {
        if (!tryToOpenInternal(player)) return;
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
        copyAllButton.reload(configFile, configSection);
        makeCopyButton.reload(configFile, configSection);
        copyComponentsDisplayDialog.reload(configFile, configSection.getConfigurationSection("copy-specific"));
        noOffHandItemInfo = new DialogButton.ButtonInfo(
            ConfigUtils.parseComponent(configFile, configSection.getString("no-off-hand-item.label"), null, null),
            configSection.isSet("no-off-hand-item.tooltip") ? ConfigUtils.parseComponent(configFile, configSection.getString("no-off-hand-item.tooltip"), null, null): null
        );
    }

    @Override
    protected void onExternalButtonPressed(DialogResponseView response, Player player) {
        if(player.getInventory().getItemInOffHand().isEmpty()) {
            returnToPrevious(player);
            return;
        }
        super.onExternalButtonPressed(response, player);
    }

    @Override
    protected List<ActionButton> getDialogButtons(Player player, ItemStack item) {
        return List.of(
            copyComponentsDisplayDialog.openActionButton(player),
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
    protected boolean tryToOpenInternal(Player player) {
        var val = super.tryToOpenInternal(player);
        if (!val) return false;
        val = !player.getInventory().getItemInOffHand().isEmpty();
        if (!val) {
            returnToPrevious(player);
        }
        return val;
    }

    @Override
    protected @Nullable DialogButton.ButtonInfo openButtonInfo(Player player) {
        if(player.getInventory().getItemInOffHand().isEmpty()) {
            return noOffHandItemInfo;
        }
        return super.openButtonInfo(player);
    }
}
