package at.iamsoccer.soccerisawesome.itemrename.dialog;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ItemLoreRenameDialog extends AbstractRenameDialog {
    public ItemLoreRenameDialog() {
        super(NamespacedKey.fromString("rename:lore"));
    }

    @Override protected String getSuggestionFromItem(ItemStack item) {
        if (item.getPersistentDataContainer().has(pdcDataKey, PersistentDataType.LIST.strings())) {
            return item.getPersistentDataContainer().get(pdcDataKey, PersistentDataType.LIST.strings()).stream().collect(Collectors.joining("\n"));
        } else {
            return "";
        }
    }

    private static @NonNull Component concatComponents(List<Component> lore) {
        var builder = Component.text();
        for (Component component : lore) {
            if (!builder.children().isEmpty()) builder.append(Component.newline());
            builder.append(component);
        }
        var comp = builder.build();
        return comp;
    }

    @Override protected @NonNull Component parseIntoPreviewComponent(Player player, String text) {
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
    protected void applyToPDC(PersistentDataContainer pdc, String input) {
        if (input.isBlank()) {
            pdc.remove(pdcDataKey);
        } else {
            pdc.set(pdcDataKey, PersistentDataType.LIST.strings(), Arrays.stream(input.split("\n")).toList());
        }
    }
}
