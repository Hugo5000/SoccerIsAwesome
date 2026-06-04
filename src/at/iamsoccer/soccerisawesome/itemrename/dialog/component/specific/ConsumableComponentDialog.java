package at.iamsoccer.soccerisawesome.itemrename.dialog.component.specific;

import at.iamsoccer.soccerisawesome.itemrename.dialog.component.AbstractDataComponentEditorDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractDialogFactory;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class ConsumableComponentDialog extends AbstractDataComponentEditorDialog<Consumable> {
    public ConsumableComponentDialog(Supplier<AbstractDialogFactory<Player>> returnDialogFactorySupplier) {
        super(returnDialogFactorySupplier, DataComponentTypes.CONSUMABLE);
    }


    @Override
    public List<DialogInput> parseResponseToInputs(@Nullable DialogResponseView response, ItemStack itemStack, @Nullable Consumable currentComponent) {
        return List.of(
            DialogInput.bool("particles", Component.text("Consume Particles"))
                .initial(currentComponent != null ? currentComponent.hasConsumeParticles() : true)
                .build()
        );
    }

    @Override
    public @Nullable Consumable parseResponseToComponent(DialogResponseView response, ItemStack itemStack, @Nullable Consumable currentComponent) {
        return Consumable.consumable()
            .hasConsumeParticles(getValue(response, "particles", true))
            .build();
    }
}
