package at.iamsoccer.soccerisawesome.itemrename.dialog.component;

import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.itemrename.ItemRenameModule;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.AbstractDialogFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractItemPreviewAndApplyDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.DialogButton;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractDataComponentEditorDialog<DataComponent> extends AbstractItemPreviewAndApplyDialog {
    protected final DataComponentType.Valued<DataComponent> dataComponentType;

    private final DialogButton<Player> resetButton;
    private final DialogButton<Player> removeButton;

    public AbstractDataComponentEditorDialog(
        Supplier<AbstractDialogFactory<Player>> returnDialogFactorySupplier,
        DataComponentType.Valued<DataComponent> dataComponentType
    ) {
        super(ItemRenameModule.createPermission(dataComponentType), returnDialogFactorySupplier);
        this.dataComponentType = dataComponentType;

        this.resetButton = newButton("reset-component", (response, player) -> {
            if (!tryOpen(player)) return;
            var item = player.getInventory().getItemInMainHand();
            item.resetData(dataComponentType);
            open(player);
        });
        this.removeButton = newButton("remove-component", (response, player) -> {
            if (!tryOpen(player)) return;
            var item = player.getInventory().getItemInMainHand();
            item.resetData(dataComponentType);
            returnToPrevious(player);
        });
    }

    public abstract List<DialogInput> parseResponseToInputs(@Nullable DialogResponseView response, ItemStack itemStack, @Nullable DataComponent currentComponent);
    public abstract @Nullable DataComponent parseResponseToComponent(DialogResponseView response, ItemStack itemStack, @Nullable DataComponent currentComponent);

    public Map.Entry<DataComponentType.Valued<DataComponent>, AbstractDataComponentEditorDialog<DataComponent>> entry() {
        return Map.entry(dataComponentType, this);
    }

    @Override
    public void reload(YamlFileConfig configFile, ConfigurationSection configSection) {
        super.reload(configFile, configSection);
    }

    @Override
    protected List<ActionButton> getDialogButtons(Player player) {
        var buttons = super.getDialogButtons(player);
        var item = player.getInventory().getItemInMainHand();
        if (item.getType().asItemType().hasDefaultData(dataComponentType)) buttons.add(resetButton.button(player));
        buttons.add(removeButton.button(player));
        return buttons;
    }

    @Override
    protected void modifyPreview(Player player, @Nullable DialogResponseView response, ItemStack item) {
        if (response == null) return;
        @Nullable var comp = parseResponseToComponent(response, item, item.getData(dataComponentType));
        if (comp != null) item.setData(dataComponentType, comp);
    }

    @Override
    protected List<DialogInput> dialogInputs(Player player, @Nullable DialogResponseView response) {
        var item = player.getInventory().getItemInMainHand();
        return parseResponseToInputs(response, item.clone(), item.getData(dataComponentType));
    }

    @Override
    protected void applyToItem(Player player, DialogResponseView response, ItemStack item) {
        @Nullable var dataComponent = parseResponseToComponent(response, item, item.getData(dataComponentType));
        if (dataComponent == null) {
            // TODO: some verification/message to the user in a dialog
        } else {
            item.setData(dataComponentType, dataComponent);
        }
    }

    protected DialogButton.ButtonInfo openButtonInfo(Player player) {
        return new DialogButton.ButtonInfo(dialogTitle(player, null), null);
    }

    @Override
    public TagResolver tagResolver(Player player, @Nullable DialogResponseView response) {
        return TagResolver.builder()
            .resolver(super.tagResolver(player, response))
            .tag("component", Tag.preProcessParsed(dataComponentType.key().asMinimalString()))
            .build();
    }

    protected final <EnumType extends Enum<EnumType>> SingleOptionDialogInput.OptionEntry createOption(Enum<EnumType> key, String name, @Nullable DialogResponseView response, @Nullable DataComponent currentComponent, boolean isDefault, Function<DataComponent, EnumType> supplier) {
        return SingleOptionDialogInput.OptionEntry.create(key.name(),
            Component.text(name),
            response != null
                ? key.name().equals(getString(response, "slot", () -> ""))
                : isDefault
                  ? currentComponent == null || key.equals(supplier.apply(currentComponent))
                  : currentComponent != null && key.equals(supplier.apply(currentComponent))
        );
    }

    protected final TextDialogInput createKeyInput(String key, String name, @Nullable DialogResponseView response, @Nullable DataComponent currentComponent, Function<DataComponent, @Nullable Key> keySupplier) {
        return DialogInput.text(key, Component.text(name))
            .initial(getString(response, key, () -> currentComponent != null && keySupplier.apply(currentComponent) != null ? keySupplier.apply(currentComponent).asString() : ""))
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
