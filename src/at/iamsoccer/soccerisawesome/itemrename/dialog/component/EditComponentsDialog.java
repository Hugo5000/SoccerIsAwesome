package at.iamsoccer.soccerisawesome.itemrename.dialog.component;

import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.IDialogFactory;
import io.papermc.paper.datacomponent.DataComponentType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;

import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class EditComponentsDialog extends AbstractDataComponentListDialog {
    public EditComponentsDialog(Permission permission, Supplier<IDialogFactory> returnFactorySupplier) {
        super(permission, returnFactorySupplier);
    }

    @Override
    protected boolean filter(DataComponentType type, ItemStack itemStack) {
        return itemStack.hasData(type);
    }
}
