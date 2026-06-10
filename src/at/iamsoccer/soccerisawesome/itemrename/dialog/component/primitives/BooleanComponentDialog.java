package at.iamsoccer.soccerisawesome.itemrename.dialog.component.primitives;

import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.AbstractDialogFactory;
import io.papermc.paper.datacomponent.DataComponentType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class BooleanComponentDialog extends ToggleComponentDialogButton<DataComponentType.Valued<Boolean>> {
    private final Function<ItemStack, Boolean> defaultSupplier;

    public BooleanComponentDialog(
        @Nullable Supplier<AbstractDialogFactory<Player>> returnDialogFactorySupplier,
        DataComponentType.Valued<Boolean> dataComponentType,
        Function<ItemStack, Boolean> defaultSupplier
    ) {
        super(dataComponentType, returnDialogFactorySupplier);
        this.defaultSupplier = defaultSupplier;
    }

    @Override
    protected void addData(ItemStack item) {
        @Nullable var defaultValue = item.getType().asItemType().getDefaultData(dataComponentType);
        if (defaultValue == null) defaultValue = defaultSupplier.apply(item);
        if (defaultValue) item.unsetData(dataComponentType);
        else item.setData(dataComponentType, true);
    }

    @Override
    protected void removeData(ItemStack item) {
        @Nullable var defaultValue = item.getType().asItemType().getDefaultData(dataComponentType);
        if (defaultValue == null) defaultValue = defaultSupplier.apply(item);
        if (!defaultValue) item.unsetData(dataComponentType);
        else item.setData(dataComponentType, false);
    }

    @Override
    protected boolean shouldAdd(ItemStack item) {
        return  !item.hasData(dataComponentType) && !defaultSupplier.apply(item)
        || item.hasData(dataComponentType) && !item.getData(dataComponentType);
    }

    public Map.Entry<DataComponentType.Valued<?>, BooleanComponentDialog> entry() {
        return Map.entry(dataComponentType, this);
    }
}
