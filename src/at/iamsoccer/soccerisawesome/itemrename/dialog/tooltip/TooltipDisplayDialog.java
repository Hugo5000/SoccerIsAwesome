package at.iamsoccer.soccerisawesome.itemrename.dialog.tooltip;

import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractButtonListDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.IDialogFactory;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import io.papermc.paper.registry.data.dialog.ActionButton;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class TooltipDisplayDialog extends AbstractButtonListDialog {
    public TooltipDisplayDialog(Permission permission, @Nullable Supplier<IDialogFactory> returnDialogFactorySupplier) {
        super(permission, returnDialogFactorySupplier);
    }

    private final SpecificTooltipDisplayDialog specificTooltipDisplayDialog = new SpecificTooltipDisplayDialog(permission, () -> this);

    private final DialogButton hideAllButton = new DialogButton("hide-all", null, (response, audience) -> {
        checkAudienceAndSetTooltip(audience, item -> TooltipDisplay.tooltipDisplay().hiddenComponents(getComponents(item)).build());
    });

    private final DialogButton showAllButton = new DialogButton("show-all", null, (response, audience) -> {
        checkAudienceAndSetTooltip(audience, item -> null);
    });

    private final DialogButton hideTooltipAction = new DialogButton(player -> {
        var item = player.getInventory().getItemInMainHand();
        @Nullable var tooltip = item.getData(DataComponentTypes.TOOLTIP_DISPLAY);
        if (tooltip == null || !tooltip.hideTooltip()) {
            return new DialogButton.ButtonInfo(
                Component.text("Hide Tooltip"), null
            );
        }
        return new DialogButton.ButtonInfo(
            Component.text("Show Tooltip"), null
        );
    }, "hide-tooltip", null, (response, audience) -> {
        checkAudienceAndSetTooltip(audience, item -> {
            @Nullable var tooltip = item.getData(DataComponentTypes.TOOLTIP_DISPLAY);
            return TooltipDisplay.tooltipDisplay().hideTooltip(tooltip == null || !tooltip.hideTooltip()).build();
        });
    });

    private void checkAudienceAndSetTooltip(Audience audience, Function<ItemStack, @Nullable TooltipDisplay> tooltipSupplier) {
        if (!(audience instanceof Player player) || !player.hasPermission(permission)) return;
        var item = player.getInventory().getItemInMainHand();
        if (item.isEmpty()) return;
        @Nullable var tooltip = tooltipSupplier.apply(item);
        if (tooltip == null) {
            if (item.getType().asItemType().hasDefaultData(DataComponentTypes.TOOLTIP_DISPLAY)) {
                item.resetData(DataComponentTypes.TOOLTIP_DISPLAY);
            } else {
                item.unsetData(DataComponentTypes.TOOLTIP_DISPLAY);
            }
        } else {
            item.setData(DataComponentTypes.TOOLTIP_DISPLAY, tooltip);
        }
        player.showDialog(create(player));
    }

    @Override
    protected List<ActionButton> getDialogButtons(Player player, ItemStack item) {
        var buttons = new ArrayList<ActionButton>(4);
        @Nullable var tooltipDisplay = item.getData(DataComponentTypes.TOOLTIP_DISPLAY);
        if (tooltipDisplay == null || !tooltipDisplay.hiddenComponents().containsAll(getComponents(item))) {
            buttons.add(hideAllButton.button(player));
        }
        if (tooltipDisplay != null && !tooltipDisplay.hiddenComponents().isEmpty()) {
            buttons.add(showAllButton.button(player));
        }
        buttons.add(hideTooltipAction.button(player));
        buttons.add(specificTooltipDisplayDialog.openActionButton(player));
        return buttons;
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
