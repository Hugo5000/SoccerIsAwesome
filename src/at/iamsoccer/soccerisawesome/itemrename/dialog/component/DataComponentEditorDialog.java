package at.iamsoccer.soccerisawesome.itemrename.dialog.component;

import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.IDialogFactory;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class DataComponentEditorDialog<DataComponent> extends AbstractDataComponentEditorDialog<DataComponent> {
    @FunctionalInterface
    public interface IDialogResponseParser<DataComponent> {
        @Nullable DataComponent parseResponse(DialogResponseView response, @Nullable DataComponent currentComponent);
    }

    @FunctionalInterface
    public interface IDialogInputProvider<DataComponent> {
        List<DialogInput> parseResponse(@Nullable DialogResponseView response, ItemStack itemStack, @Nullable DataComponent currentComponent);
    }

    private final IDialogInputProvider<DataComponent> inputs;
    private final IDialogResponseParser<DataComponent> parseResponse;

    public DataComponentEditorDialog(
        Supplier<IDialogFactory> returnDialogFactorySupplier,
        DataComponentType.Valued<DataComponent> dataComponentType,
        IDialogInputProvider<DataComponent> inputs,
        IDialogResponseParser<DataComponent> parseResponse
    ) {
        super(returnDialogFactorySupplier, dataComponentType);
        this.inputs = inputs;
        this.parseResponse = parseResponse;
    }

    @Override
    public List<DialogInput> parseResponseToInputs(@Nullable DialogResponseView response, ItemStack itemStack, @Nullable DataComponent currentComponent) {
        return inputs.parseResponse(response, itemStack, currentComponent);
    }

    @Override
    public @Nullable DataComponent parseResponseToComponent(DialogResponseView response, ItemStack itemStack, @Nullable DataComponent currentComponent) {
        return parseResponse.parseResponse(response, currentComponent);
    }

}
