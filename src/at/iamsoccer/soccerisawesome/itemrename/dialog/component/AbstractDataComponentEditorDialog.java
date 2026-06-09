package at.iamsoccer.soccerisawesome.itemrename.dialog.component;

import at.iamsoccer.soccerisawesome.itemrename.ItemRenameModule;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractDialogFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractItemPreviewAndApplyDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.buttons.DialogButton;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.item.Equippable;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractDataComponentEditorDialog<DataComponent> extends AbstractItemPreviewAndApplyDialog {
    protected final DataComponentType.Valued<DataComponent> dataComponentType;

    public AbstractDataComponentEditorDialog(
        Supplier<AbstractDialogFactory<Player>> returnDialogFactorySupplier,
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
    protected List<DialogBody> body(List<DialogBody> body, Player player, @Nullable DialogResponseView responseView, ItemStack item) {
        if (responseView != null) {
            @Nullable var comp = parseResponseToComponent(responseView, item, item.getData(dataComponentType));
            if (comp != null) item.setData(dataComponentType, comp);
        }
        body.add(DialogBody.item(item).build());
        return body;
    }

    @Override
    protected List<DialogInput> inputs(Player player, @Nullable DialogResponseView responseView, ItemStack item) {
        return parseResponseToInputs(responseView, item.clone(), item.getData(dataComponentType));
    }

    @Override
    protected void onApply(DialogResponseView response, Player player, ItemStack item) {
        @Nullable
        var dataComponent = parseResponseToComponent(response, item, item.getData(dataComponentType));
        if (dataComponent == null) {
            // TODO: some verification/message to the user in a dialog
        } else {
            applyToItem(item, dataComponent);
        }
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

    protected final <EnumType extends Enum<EnumType>> SingleOptionDialogInput.OptionEntry createOption(Enum<EnumType> key, String name, @Nullable DialogResponseView response, @Nullable DataComponent currentComponent, boolean isDefault, Function<DataComponent, EnumType> supplier) {
        return SingleOptionDialogInput.OptionEntry.create(key.name(),
            Component.text(name),
            response != null
                ? key.equals(getValue(response, "slot", ""))
                : isDefault
                  ? currentComponent == null || supplier.apply(currentComponent) == key
                  : currentComponent != null && supplier.apply(currentComponent) == key
        );
    }

    protected final TextDialogInput createKeyInput(String key, String name, @Nullable DialogResponseView response, @Nullable DataComponent currentComponent, Function<DataComponent, @Nullable Key> keySupplier) {
        return DialogInput.text(key, Component.text(name))
            .initial(getValue(response, key, currentComponent != null && keySupplier.apply(currentComponent) != null ? keySupplier.apply(currentComponent).asString() : ""))
            .multiline(TextDialogInput.MultilineOptions.create(null, 20))
            .maxLength(256)
            .build();
    }

    protected static String parseEntities(@Nullable RegistryKeySet<EntityType> entities) {
        if (entities == null) return "";
        return entities.values()
            .stream()
            .map(type -> type.key().asMinimalString())
            .collect(Collectors.joining("\n"));
    }

    protected static @Nullable RegistryKeySet<EntityType> parseEntities(String allowedEntities) {
        if (allowedEntities.isBlank()) return null;
        var entities = Arrays.stream(allowedEntities.split("\n"))
            .map(String::trim)
            .<EntityType>mapMulti((str, consumer) -> {
                try {
                    consumer.accept(EntityType.valueOf(str.toUpperCase()));
                } catch (IllegalArgumentException ignored) {
                }
            }).toList();
        return RegistrySet.keySetFromValues(RegistryKey.ENTITY_TYPE, entities);
    }
}
