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
                    var deserialized = PlainTextComponentSerializer.plainText().serialize(serializerFor(player).deserialize(res.rawText())).hashCode();
                    if (res.plainHash() != deserialized) isDifferent = true;
                }
                ++index;
                if (res.isUnknown() || res.isServerSigned() || res.isPlayerSigned() && !player.getUniqueId().equals(res.signeeUUID())) continue;
                ++lastIndex;
                var text = res.isPlayerSigned() ? "<signed>" + res.rawText() : res.rawText();
                if (index == lastIndex) {
                    lines.add(text);
                } else {
                    lines.add(index + ":" + text);
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
            .filter(comp -> {
                var signed = SignedComponent.parse(comp);
                return !(player.getUniqueId().equals(signed.signeeUUID()));
            })
            .collect(Collectors.toCollection(ArrayList::new));
        if (input.isBlank()) {
            if (lore.isEmpty()) item.resetData(DataComponentTypes.LORE);
            item.setData(DataComponentTypes.LORE, ItemLore.lore().lines(lore).build());
        } else {
            AtomicInteger rollingIndex = new AtomicInteger();
            Arrays.stream(input.split("\n"))
                .forEach(line -> {
                    var semiColonIndex = line.indexOf(':');
                    if (semiColonIndex >= 0) {
                        try {
                            var newIndex = Integer.parseInt(line.substring(0, semiColonIndex)) ;
                            line = line.substring(semiColonIndex + 1);
                            if (newIndex < 0) newIndex = lore.size() + newIndex + 1;
                            else newIndex = newIndex - 1;
                            rollingIndex.set(newIndex);
                        } catch (NumberFormatException e) {
                        }
                    }
                    if (line.startsWith("<sign>") || line.startsWith("<signed>")) {
                        lore.add(rollingIndex.getAndIncrement(), SignedComponent.sign(player.getUniqueId(), line.substring(line.indexOf('>') + 1), serializerFor(player)).component());
                    } else {
                        Component component = SignedComponent.unSigned(line, serializerFor(player)).component();
                        lore.add(rollingIndex.getAndIncrement(), component);
                    }
                });
            item.setData(DataComponentTypes.LORE, ItemLore.lore().lines(lore).build());
        }
    }

    @Override
    protected boolean hasSignTag() {
        return true;
    }
}
