package at.iamsoccer.soccerisawesome.itemrename.dialog.rename;

import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.IDialogFactory;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class ItemLoreRenameDialog extends AbstractRenameDialog {
    public ItemLoreRenameDialog(@Nullable Permission permission, @Nullable Supplier<IDialogFactory> returnDialogSupplier) {
        super(NamespacedKey.fromString("rename:lore"), permission, returnDialogSupplier);
    }

    @Override
    protected SuggestionResult getSuggestionFromItem(Player player, ItemStack item) {
        final String suggestion;
        final String plain;
        if (item.getPersistentDataContainer().has(pdcDataKey, PersistentDataType.TAG_CONTAINER)) {
            var container = item.getPersistentDataContainer().get(pdcDataKey, PersistentDataType.TAG_CONTAINER);
            suggestion = String.join("\n", container.get(rawDataKey, PersistentDataType.LIST.strings()));
            plain = String.join("\n", container.get(plainDataKey, PersistentDataType.LIST.strings()));
        } else {
            return new SuggestionResult("", false);
        }
        var deserialized = PlainTextComponentSerializer.plainText().serialize(parseLine(player, suggestion));
        return new SuggestionResult(suggestion, !deserialized.equals(plain));
    }

    @Override
    protected boolean isDifferentThanExpected(Player player, ItemStack item) {
        return getSuggestionFromItem(player, item).isDifferent();
    }

    private static Component concatComponents(List<Component> lore) {
        var builder = Component.text();
        for (Component component : lore) {
            if (!builder.children().isEmpty()) builder.append(Component.newline());
            builder.append(component);
        }
        return builder.build();
    }

    @Override protected Component parseIntoPreviewComponent(Player player, String text) {
        var builder = Component.text();
        Arrays.stream(text.split("\n"))
            .map(line -> parseLine(player, line))
            .forEach(comp -> {
                if (!builder.children().isEmpty()) builder.append(Component.newline());
                builder.append(comp);
            });
        return builder.build();
    }

    @Override
    protected void applyToItem(Player player, String input, ItemStack item) {
        // TODO: add perms for lore position, and make it not overwrite non rename lore
        // TODO: limit lore lines and length
        if (input.isBlank()) {
            item.resetData(DataComponentTypes.LORE);
        } else {
            item.setData(DataComponentTypes.LORE, ItemLore.lore()
                .lines(Arrays.stream(input.split("\n"))
                    .map(line -> parseLine(player, line))
                    .toList())
                .build()
            );
        }
    }

    @Override
    protected void applyToPDC(Player player, PersistentDataContainer pdc, String input) {
        if (input.isBlank()) {
            pdc.remove(pdcDataKey);
        } else {

            var container = pdc.getAdapterContext().newPersistentDataContainer();
            container.set(rawDataKey, PersistentDataType.LIST.strings(), Arrays.stream(input.split("\n")).toList());
            container.set(plainDataKey, PersistentDataType.LIST.strings(), Arrays.stream(input.split("\n"))
                .map(line -> PlainTextComponentSerializer.plainText().serialize(parseLine(player, line)))
                .toList()
            );
            pdc.set(pdcDataKey, PersistentDataType.TAG_CONTAINER, container);
        }

    }
}
