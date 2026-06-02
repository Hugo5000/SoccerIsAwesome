package at.iamsoccer.soccerisawesome.itemrename.dialog.component.specific;

import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.IDialogFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.component.AbstractDataComponentEditorDialog;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import net.kyori.adventure.text.Component;
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
        Supplier<IDialogFactory> returnDialogFactorySupplier,
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
    public List<? extends DialogInput> parseResponseToInputs(@Nullable DialogResponseView response, ItemStack itemStack, @Nullable Integer currentComponent) {
        return List.of(
            DialogInput.numberRange("value", Component.text(dataComponentType.key().asMinimalString()), minValueSupplier.apply(itemStack), maxValueSupplier.apply(itemStack))
                .initial(getValue(response, "value", currentComponent != null ? currentComponent.floatValue() : defaultValueSupplier.apply(itemStack)))
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
