package at.iamsoccer.soccerisawesome.itemrename.dialog.component.primitives;

import at.iamsoccer.soccerisawesome.itemrename.dialog.component.AbstractDataComponentEditorDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.AbstractDialogFactory;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class KeyComponentDialog extends AbstractDataComponentEditorDialog<Key> {
    public KeyComponentDialog(
        Supplier<AbstractDialogFactory<Player>> returnDialogFactorySupplier,
        DataComponentType.Valued<Key> integerDataComponentType
    ) {
        super(returnDialogFactorySupplier, integerDataComponentType);
    }

    @Override
    public List<DialogInput> parseResponseToInputs(@Nullable DialogResponseView response, ItemStack itemStack, @Nullable Key currentComponent) {
        return List.of(createKeyInput("key", "Key", response, currentComponent, key -> currentComponent));
    }

    @Override
    public @Nullable Key parseResponseToComponent(DialogResponseView response, ItemStack itemStack, @Nullable Key currentComponent) {
        var key = getString(response, "key", () -> "");
        if (key.isBlank() || !Key.parseable(key)) return null;
        return Key.key(key);
    }
}
