package at.iamsoccer.soccerisawesome.itemrename.dialog.component;

import at.iamsoccer.soccerisawesome.itemrename.ItemRenameModule;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractPreviewAndApplyEditorDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.IDialogFactory;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractDataComponentEditorDialog<DataComponent> extends AbstractPreviewAndApplyEditorDialog {
    protected final DataComponentType.Valued<DataComponent> dataComponentType;

    public AbstractDataComponentEditorDialog(
        Supplier<IDialogFactory> returnDialogFactorySupplier,
        DataComponentType.Valued<DataComponent> dataComponentType
    ) {
        super(ItemRenameModule.createPermission(dataComponentType), returnDialogFactorySupplier);
        this.dataComponentType = dataComponentType;
    }

    public abstract List<DialogInput> parseResponseToInputs(@Nullable DialogResponseView response, ItemStack itemStack, @Nullable DataComponent currentComponent);
    public abstract @Nullable DataComponent parseResponseToComponent(DialogResponseView response, ItemStack itemStack, @Nullable DataComponent currentComponent);

    public Map.Entry<DataComponentType.Valued<DataComponent>, AbstractDataComponentEditorDialog<DataComponent>> entry() {
        return Map.entry(dataComponentType, this);
    }

    @Override
    protected List<DialogBody> body(List<DialogBody> body, Player player, @Nullable DialogResponseView responseView) {
        var item = player.getInventory().getItemInMainHand().clone();
        if (responseView != null) {
            @Nullable var comp = parseResponseToComponent(responseView, item, item.getData(dataComponentType));
            if (comp != null) item.setData(dataComponentType, comp);
        }
        body.add(DialogBody.item(item).build());
        return body;
    }

    @Override
    protected List<DialogInput> inputs(Player player, @Nullable DialogResponseView responseView) {
        var item = player.getInventory().getItemInMainHand().clone();
        return parseResponseToInputs(responseView, item, item.getData(dataComponentType));
    }

    @Override
    protected void onApply(DialogResponseView response, Player player) {
        var item = player.getInventory().getItemInMainHand();
        @Nullable
        var dataComponent = parseResponseToComponent(response, item, item.getData(dataComponentType));
        if (dataComponent == null) {
            // TODO: some verification/message to the user in a dialog
        } else {
            applyToItem(item, dataComponent);
        }
    }

    @Override
    protected void onPreview(DialogResponseView response, Player player) {
        create(player, response);
    }

    private void applyToItem(ItemStack item, DataComponent data) {
        item.setData(dataComponentType, data);
    }

    @Override
    protected DialogButton.ButtonInfo openButtonInfo(Player player) {
        return new DialogButton.ButtonInfo(titleProvider(), null);
    }

    @Override
    protected Component titleProvider() {
        return Component.text(dataComponentType.key().asMinimalString());
    }
}
