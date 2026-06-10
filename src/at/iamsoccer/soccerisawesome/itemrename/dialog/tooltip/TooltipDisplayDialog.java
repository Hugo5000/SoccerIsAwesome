package at.iamsoccer.soccerisawesome.itemrename.dialog.tooltip;

import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractButtonListDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.AbstractDialogFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.DialogButton;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import io.papermc.paper.registry.data.dialog.ActionButton;
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
    private DialogButton.UnparsedButtonInfo<Player> hideTooltipInfo;
    private DialogButton.UnparsedButtonInfo<Player> showTooltipInfo;

    private final SpecificTooltipDisplayDialog specificTooltipDisplayDialog = new SpecificTooltipDisplayDialog(permission, () -> this);

    private final DialogButton<Player> hideAllButton = newButton("hide-all", (response, audience) -> {
        checkAudienceAndSetTooltip(audience, item -> TooltipDisplay.tooltipDisplay().hiddenComponents(getComponents(item)).build());
    });

    private final DialogButton<Player> showAllButton = newButton("show-all", (response, audience) -> {
        checkAudienceAndSetTooltip(audience, item -> null);
    });

    private final DialogButton<Player> hideTooltipAction = newButton("hide-tooltip", (response, audience) -> {
        checkAudienceAndSetTooltip(audience, item -> {
            @Nullable var tooltip = item.getData(DataComponentTypes.TOOLTIP_DISPLAY);
            return TooltipDisplay.tooltipDisplay().hideTooltip(tooltip == null || !tooltip.hideTooltip()).build();
        });
    });

    public TooltipDisplayDialog(@Nullable Permission permission, @Nullable Supplier<AbstractDialogFactory<Player>> returnDialogFactorySupplier) {
        super(permission, returnDialogFactorySupplier);
    }

    @Override
    protected DialogButton.IButtonInfoSupplier<Player> buttonInfoSupplier(String name) {
        if(name.equals("hide-tooltip")) {
            return player -> {
                var item = player.getInventory().getItemInMainHand();
                @Nullable var tooltip = item.getData(DataComponentTypes.TOOLTIP_DISPLAY);
                if (tooltip == null || !tooltip.hideTooltip()) hideTooltipInfo.parse(this, player, null);
                return showTooltipInfo.parse(this, player, null);
            };
        }
        return super.buttonInfoSupplier(name);
    }

    private void checkAudienceAndSetTooltip(Player player, Function<ItemStack, @Nullable TooltipDisplay> tooltipSupplier) {
        if (!tryOpen(player)) return;
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
        open(player);
    }

    @Override
    protected List<ActionButton> getDialogButtons(Player player) {
        var item = player.getInventory().getItemInMainHand();
        var buttons = new ArrayList<ActionButton>(4);
        @Nullable var tooltipDisplay = item.getData(DataComponentTypes.TOOLTIP_DISPLAY);
        if (tooltipDisplay == null || !tooltipDisplay.hiddenComponents().containsAll(getComponents(item))) {
            buttons.add(hideAllButton.button(player));
        }
        if (tooltipDisplay != null && !tooltipDisplay.hiddenComponents().isEmpty()) {
            buttons.add(showAllButton.button(player));
        }
        buttons.add(hideTooltipAction.button(player));
        buttons.add(specificTooltipDisplayDialog.externalButton().button(player));
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
        specificTooltipDisplayDialog.reload(configFile, configSection.getConfigurationSection("hide-specific"));
        hideTooltipInfo = DialogButton.parseFromConfigSection(configFile, configSection, "hide-button", null);
        showTooltipInfo = DialogButton.parseFromConfigSection(configFile, configSection, "show-button", null);
    }
}
