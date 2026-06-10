package at.iamsoccer.soccerisawesome.itemrename.dialog.component.primitives;

import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.AbstractDialogFactory;
import io.papermc.paper.datacomponent.DataComponentType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class NonValuedComponentEditorDialog extends ToggleComponentDialogButton<DataComponentType.NonValued> {
    public NonValuedComponentEditorDialog(
        @Nullable Supplier<AbstractDialogFactory<Player>> returnDialogFactorySupplier,
        DataComponentType.NonValued dataComponentType
    ) {
        super(dataComponentType, returnDialogFactorySupplier);
    }

    @Override
    protected void addData(ItemStack item) {
        item.setData(dataComponentType);
    }

    @Override
    protected void removeData(ItemStack item) {
        item.unsetData(dataComponentType);
    }
}
