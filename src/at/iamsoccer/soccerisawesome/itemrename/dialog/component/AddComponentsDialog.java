package at.iamsoccer.soccerisawesome.itemrename.dialog.component;

import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.IDialogFactory;
import io.papermc.paper.datacomponent.DataComponentType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.checkerframework.checker.index.qual.Positive;

import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class AddComponentsDialog extends AbstractDataComponentListDialog {
    public AddComponentsDialog(Permission permission, Supplier<IDialogFactory> returnFactorySupplier) {
        super(permission, returnFactorySupplier);
    }

    @Override
    protected boolean filter(DataComponentType type, ItemStack itemStack) {
        return !itemStack.hasData(type);
    }

    protected @Positive int getColumns() {
        return 2;
    }
}
