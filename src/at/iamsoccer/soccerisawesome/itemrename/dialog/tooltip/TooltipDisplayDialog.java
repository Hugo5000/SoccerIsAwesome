package at.iamsoccer.soccerisawesome.itemrename.dialog.tooltip;

import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractButtonListDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.IDialogFactory;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import io.papermc.paper.registry.data.dialog.ActionButton;
import net.kyori.adventure.audience.Audience;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class TooltipDisplayDialog extends AbstractButtonListDialog {
    public TooltipDisplayDialog(Permission permission, Supplier<IDialogFactory> returnDialogFactorySupplier) {
        super(permission, returnDialogFactorySupplier);
    }

    private final SpecificTooltipDisplayDialog specificTooltipDisplayDialog = new SpecificTooltipDisplayDialog(permission, () -> this);

    private final DialogButton hideAllButton = new DialogButton("hide-all", null, (response, audience) -> {
        checkAudienceAndSetTooltip(audience, item -> TooltipDisplay.tooltipDisplay().hiddenComponents(getComponents(item)).build());
    });

    private final DialogButton showAllButton = new DialogButton("show-all", null, (response, audience) -> {
        checkAudienceAndSetTooltip(audience, item -> TooltipDisplay.tooltipDisplay().build());
    });

    private final DialogButton hideTooltipAction = new DialogButton("hide-tooltip", null, (response, audience) -> {
        checkAudienceAndSetTooltip(audience, item -> TooltipDisplay.tooltipDisplay().hideTooltip(true).build());
    });

    private void checkAudienceAndSetTooltip(Audience audience, Function<ItemStack, TooltipDisplay> tooltipSupplier) {
        if (!(audience instanceof Player player) || !player.hasPermission(permission)) return;
        var item = player.getInventory().getItemInMainHand();
        if (item.isEmpty()) return;
        item.setData(DataComponentTypes.TOOLTIP_DISPLAY, tooltipSupplier.apply(item));
        player.showDialog(create(player));
    }

    @Override
    protected List<ActionButton> getDialogButtons(Player player, ItemStack item) {
        // TODO: hide some buttons if they are already in effect
        return List.of(
            hideAllButton.button(),
            showAllButton.button(),
            hideTooltipAction.button(),
            specificTooltipDisplayDialog.openActionButton()
        );
    }

    private Set<DataComponentType> getComponents(ItemStack itemStack) {
        var set = new HashSet<DataComponentType>();
        set.addAll(itemStack.getDataTypes());
        set.addAll(itemStack.getType().asItemType().getDefaultDataTypes());
        return set;
    }

    @Override
    public void reload(YamlFileConfig configFile, ConfigurationSection configSection) {
        super.reload(configFile, configSection);
        hideAllButton.reload(configFile, configSection);
        showAllButton.reload(configFile, configSection);
        hideTooltipAction.reload(configFile, configSection);
        specificTooltipDisplayDialog.reload(configFile, configSection.getConfigurationSection("hide-specific"));
    }
}
