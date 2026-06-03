package at.iamsoccer.soccerisawesome.itemrename.dialog.tooltip;

import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractBasicDialogFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.IDialogFactory;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class SpecificTooltipDisplayDialog extends AbstractBasicDialogFactory {
    public SpecificTooltipDisplayDialog(Permission permission, @Nullable Supplier<IDialogFactory> returnDialogFactorySupplier) {
        super(permission, returnDialogFactorySupplier);
    }

    private final DialogButton applyButton = new DialogButton("apply", "dialog.default.apply", (response, audience) -> {
        if (!(audience instanceof Player player) || !player.hasPermission(permission)) return;
        var hiddenComponents = new HashSet<DataComponentType>();
        var item = player.getInventory().getItemInMainHand();
        for (var comp : getComponents(item)) {
            if (response.getBoolean(getCompKey(comp))) {
                hiddenComponents.add(comp);
            }
        }
        var tooltip = TooltipDisplay.tooltipDisplay().hiddenComponents(hiddenComponents).build();
        item.setData(DataComponentTypes.TOOLTIP_DISPLAY, tooltip);
        returnToPrevious(audience);
    });

    @Override
    public DialogLike create(Player player) {
        var item = player.getInventory().getItemInMainHand();
        var inputs = new ArrayList<DialogInput>();
        var current = item.getDataOrDefault(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().build());
        getComponents(item).stream()
            .map(dataType -> DialogInput.bool(getCompKey(dataType), Component.text("Hide " + dataType.key().asMinimalString()))
                .initial(current.hiddenComponents().contains(dataType))
                .build()
            ).forEach(inputs::add);
        return createDialog(infoFields -> infoFields, inputs, closeButton -> DialogType.confirmation(
            applyButton.button(player),
            closeButton.button(player)
        ));
    }

    @Override
    public void reload(YamlFileConfig configFile, ConfigurationSection configSection) {
        super.reload(configFile, configSection);
        applyButton.reload(configFile, configSection);
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
