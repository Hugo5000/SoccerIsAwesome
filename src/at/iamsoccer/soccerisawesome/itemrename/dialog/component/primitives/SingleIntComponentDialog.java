package at.iamsoccer.soccerisawesome.itemrename.dialog.component.primitives;

import at.iamsoccer.soccerisawesome.itemrename.dialog.component.AbstractDataComponentEditorDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractDialogFactory;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class SingleIntComponentDialog extends AbstractDataComponentEditorDialog<Integer> {
    @FunctionalInterface
    public interface INumberFromItemSupplier {
        Integer apply(ItemStack item);
    }

    private final INumberFromItemSupplier defaultValueSupplier;
    private final INumberFromItemSupplier minValueSupplier;
    private final INumberFromItemSupplier maxValueSupplier;

    public SingleIntComponentDialog(
        Supplier<AbstractDialogFactory<Player>> returnDialogFactorySupplier,
        DataComponentType.Valued<Integer> integerDataComponentType,
        INumberFromItemSupplier defaultValueSupplier,
        INumberFromItemSupplier minValueSupplier,
        INumberFromItemSupplier maxValueSupplier
    ) {
        super(returnDialogFactorySupplier, integerDataComponentType);
        this.defaultValueSupplier = defaultValueSupplier;
        this.minValueSupplier = minValueSupplier;
        this.maxValueSupplier = maxValueSupplier;
    }

    @Override
    public List<DialogInput> parseResponseToInputs(@Nullable DialogResponseView response, ItemStack itemStack, @Nullable Integer currentComponent) {
        int min = minValueSupplier.apply(itemStack);
        int max = maxValueSupplier.apply(itemStack);
        return List.of(
            DialogInput.numberRange("value", Component.text(dataComponentType.key().asMinimalString()), min, max)
                .initial(Math.max(min, Math.min(max,
                    getValue(response, "value", currentComponent != null ? currentComponent.floatValue() : defaultValueSupplier.apply(itemStack))
                )))
                .step(1f)
                .build()
        );
    }

    @Override
    public @Nullable Integer parseResponseToComponent(DialogResponseView response, ItemStack itemStack, @Nullable Integer currentComponent) {
        @Nullable Float value = response.getFloat("value");
        if (value == null) return null;
        var intValue = value.intValue();
        if (intValue < minValueSupplier.apply(itemStack) || intValue > maxValueSupplier.apply(itemStack)) return null;
        return intValue;
    }
}
