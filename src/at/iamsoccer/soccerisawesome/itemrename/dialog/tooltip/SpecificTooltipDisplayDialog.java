package at.iamsoccer.soccerisawesome.itemrename.dialog.tooltip;

import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.AbstractDialogFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractItemDialogFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.DialogButton;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
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
public class SpecificTooltipDisplayDialog extends AbstractItemDialogFactory {
    private final DialogButton<Player> applyButton = newButton("apply", (response, player) -> {
        if (!tryOpen(player)) return;
        var hiddenComponents = new HashSet<DataComponentType>();
        var item = player.getInventory().getItemInMainHand();
        for (var comp : getComponents(item)) {
            if (Boolean.TRUE.equals(response.getBoolean(getCompKey(comp)))) {
                hiddenComponents.add(comp);
            }
        }
        var tooltip = TooltipDisplay.tooltipDisplay().hiddenComponents(hiddenComponents).build();
        item.setData(DataComponentTypes.TOOLTIP_DISPLAY, tooltip);
        returnToPrevious(player);
    });

    public SpecificTooltipDisplayDialog(@Nullable Permission permission, @Nullable Supplier<AbstractDialogFactory<Player>> returnDialogFactorySupplier) {
        super(permission, returnDialogFactorySupplier);
    }

    @Override
    protected List<DialogInput> dialogInputs(Player player, @Nullable DialogResponseView response) {
        var item = player.getInventory().getItemInMainHand();
        var current = item.getDataOrDefault(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().build());
        return getComponents(item).stream()
            .<DialogInput>map(dataType -> DialogInput.bool(getCompKey(dataType), Component.text("Hide " + dataType.key().asMinimalString()))
                .initial(current.hiddenComponents().contains(dataType))
                .build()
            ).toList();
    }

    @Override
    protected DialogType dialogType(DialogButton<Player> closeButton, Player player, @Nullable DialogResponseView response) {
        return DialogType.confirmation(
            applyButton.button(player),
            closeButton.button(player)
        );
    }

    @Override
    public void reload(YamlFileConfig configFile, ConfigurationSection configSection) {
        super.reload(configFile, configSection);
    }

    private Set<DataComponentType> getComponents(ItemStack itemStack) {
        var set = new HashSet<DataComponentType>();
        set.addAll(itemStack.getDataTypes());
        set.addAll(itemStack.getType().asItemType().getDefaultDataTypes());
        return set;
    }

    private static String getCompKey(DataComponentType comp) {
        return comp.key().asString().replace(":", "_");
    }

}
