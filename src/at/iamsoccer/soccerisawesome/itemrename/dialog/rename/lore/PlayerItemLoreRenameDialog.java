package at.iamsoccer.soccerisawesome.itemrename.dialog.rename.lore;

import at.iamsoccer.soccerisawesome.itemrename.dialog.rename.AbstractRenameDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.rename.SignedComponent;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.AbstractDialogFactory;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class PlayerItemLoreRenameDialog extends AbstractRenameDialog {
    public PlayerItemLoreRenameDialog(@Nullable Permission permission, @Nullable Supplier<AbstractDialogFactory<Player>> returnDialogSupplier) {
        super(permission, returnDialogSupplier);
    }

    @Override
    protected SuggestionResult getSuggestionFromItem(Player player, ItemStack item) {
        boolean isDifferent = false;
        if (item.hasData(DataComponentTypes.LORE)) {
            int lastIndex = 0;
            int index = 0;
            List<Component> lore = item.getData(DataComponentTypes.LORE).lines();
            List<String> lines = new ArrayList<>(lore.size());
            for (var line : lore) {
                var res = SignedComponent.parse(line);
                if (!isDifferent) {
                    var deserialized = PlainTextComponentSerializer.plainText().serialize(SignedComponent.sign(player, res.rawText(), serializerFor(player))).hashCode();
                    if (res.plainHash() != deserialized) isDifferent = true;
                }
                ++index;
                if (!player.getUniqueId().equals(res.signeeUUID())) continue;
                ++lastIndex;
                if (index == lastIndex) {
                    lines.add(res.rawText());
                } else {
                    lines.add(index + ":" + res.rawText());
                    lastIndex = index;
                }
            }
            return new SuggestionResult(lines.stream().collect(Collectors.joining("\n")), isDifferent);
        } else {
            return new SuggestionResult("", false);
        }
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

    @Override
    protected Component parseIntoPreviewComponent(Player player, String text) {
        var builder = Component.text();
        Arrays.stream(text.split("\n"))
            .map(line -> super.parseIntoPreviewComponent(player, line))
            .forEach(comp -> {
                if (!builder.children().isEmpty()) builder.append(Component.newline());
                builder.append(comp);
            });
        return builder.build();
    }

    @Override
    protected void applyToItem(Player player, String input, ItemStack item) {
        // TODO: add perms for lore position
        // TODO: limit lore lines and length
        var lore = item.getDataOrDefault(DataComponentTypes.LORE, ItemLore.lore().build()).lines()
            .stream()
            .filter(comp -> !player.getUniqueId().equals(SignedComponent.parse(comp).signeeUUID()))
            .collect(Collectors.toCollection(ArrayList::new));
        if (input.isBlank()) {
            if (lore.isEmpty()) item.resetData(DataComponentTypes.LORE);
            item.setData(DataComponentTypes.LORE, ItemLore.lore().lines(lore).build());
        } else {
            AtomicInteger rollingIndex = new AtomicInteger();
            Arrays.stream(input.split("\n"))
                .forEach(line -> {
                    var semiColonIndex = line.indexOf(':');
                    if(semiColonIndex >= 0) {
                        try {
                            var newIndex = Integer.parseInt(line.substring(0, semiColonIndex));
                            line = line.substring(semiColonIndex + 1);
                            if (newIndex < 0) newIndex = lore.size() + newIndex + 1;
                            rollingIndex.set(newIndex);
                            lore.add(rollingIndex.get(), SignedComponent.sign(player, line, serializerFor(player)));
                            return;
                        } catch (NumberFormatException e) {
                        }
                    }
                    lore.add(rollingIndex.getAndIncrement(), SignedComponent.sign(player, line, serializerFor(player)));
                });
            item.setData(DataComponentTypes.LORE, ItemLore.lore().lines(lore).build());
        }
    }
}
