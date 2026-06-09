package at.iamsoccer.soccerisawesome.itemrename.dialog.component.specific;

import at.iamsoccer.soccerisawesome.itemrename.dialog.component.AbstractDataComponentEditorDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractDialogFactory;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Equippable;
import io.papermc.paper.datacomponent.item.FoodProperties;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class FoodComponentDialog extends AbstractDataComponentEditorDialog<FoodProperties> {
    public FoodComponentDialog(Supplier<AbstractDialogFactory<Player>> returnDialogFactorySupplier) {
        super(returnDialogFactorySupplier, DataComponentTypes.FOOD);
    }

    @Override
    public List<DialogInput> parseResponseToInputs(@Nullable DialogResponseView response, ItemStack itemStack, @Nullable FoodProperties currentComponent) {
        return List.of(
            DialogInput.numberRange("nutrition", Component.text("Nutrition"), 0, 20)
                .initial(getValue(response, "nutrition", currentComponent != null ? currentComponent.nutrition() : 0f))
                .step(1f).width(200).build(),
            DialogInput.numberRange("saturation", Component.text("Saturation"), 0, 25)
                .initial(getValue(response, "saturation", currentComponent != null ? currentComponent.saturation() : 0f))
                .step(0.1f).width(200).build(),
            DialogInput.bool("can_always_eat", Component.text("Can always be eaten"))
                .initial(getValue(response, "can_always_eat", currentComponent != null ? currentComponent.canAlwaysEat() : false))
                .build()
        );
    }

    @Override
    public @Nullable FoodProperties parseResponseToComponent(DialogResponseView response, ItemStack itemStack, @Nullable FoodProperties currentComponent) {
        var equippable = FoodProperties.food()
            .canAlwaysEat(getValue(response, "can_always_eat", false))
            .nutrition(getValue(response, "nutrition", 0f).intValue())
            .saturation(getValue(response, "saturation", 0f));
        return equippable.build();
    }
}
