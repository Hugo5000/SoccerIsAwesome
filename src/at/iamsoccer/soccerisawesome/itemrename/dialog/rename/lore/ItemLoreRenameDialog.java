package at.iamsoccer.soccerisawesome.itemrename.dialog.rename.lore;

import at.iamsoccer.soccerisawesome.itemrename.dialog.rename.AbstractRenameDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.rename.SignedComponent;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.AbstractDialogFactory;
import com.google.common.collect.HashBiMap;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.dialog.DialogResponseView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class ItemLoreRenameDialog extends AbstractRenameDialog {
    private static HashBiMap<UUID, String> UUID_NAME_MAP = HashBiMap.create();

    public ItemLoreRenameDialog(@Nullable Permission permission, @Nullable Supplier<AbstractDialogFactory<Player>> returnDialogSupplier) {
        super(permission, returnDialogSupplier);
    }

    @Override
    protected SuggestionResult getSuggestionFromItem(Player player, ItemStack item) {
        boolean isDifferent = false;
        if (item.hasData(DataComponentTypes.LORE)) {
            List<Component> lore = item.getData(DataComponentTypes.LORE).lines();
            List<String> lines = new ArrayList<>(lore.size());
            for (var line : lore) {
                var res = SignedComponent.parse(line);
                if (!isDifferent) {
                    var deserialized = PlainTextComponentSerializer.plainText().serialize(SignedComponent.sign(player, res.rawText(), serializerFor(player))).hashCode();
                    if (res.plainHash() != deserialized) isDifferent = true;
                }
                if (res.isSigned()) {
                    if (!UUID_NAME_MAP.containsKey(res.signeeUUID())) {
                        String name = res.signeeName();
                        if (!UUID_NAME_MAP.inverse().containsKey(name)) {
                            UUID_NAME_MAP.put(res.signeeUUID(), name);
                        } else {
                            int i = 2;
                            while (true) {
                                if (!UUID_NAME_MAP.inverse().containsKey(name + "-" + i)) {
                                    UUID_NAME_MAP.put(res.signeeUUID(), name + "-" + i);
                                    break;
                                }
                            }
                        }
                    }
                    lines.add("<owner:%s>%s".formatted(UUID_NAME_MAP.get(res.signeeUUID()), res.rawText()));
                } else {
                    lines.add(res.rawText());
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
        if (input.isBlank()) {
            item.resetData(DataComponentTypes.LORE);
        } else {
            var lore = Arrays.stream(input.split("\n"))
                .map(line -> {
                    var semiColonIndex = line.indexOf(':');
                    @Nullable UUID uuid = null;
                    if (semiColonIndex >= 0 && line.substring(0, semiColonIndex).equals("<owner")) {
                        var name = line.substring(semiColonIndex + 1, line.indexOf('>', semiColonIndex));
                        if (UUID_NAME_MAP.inverse().containsKey(name)) {
                            uuid = UUID_NAME_MAP.inverse().get(name);
                        } else {
                            @Nullable var offlinePlayer = Bukkit.getOfflinePlayerIfCached(name);
                            if (offlinePlayer != null) {
                                uuid = offlinePlayer.getUniqueId();
                                UUID_NAME_MAP.put(uuid, name);
                            }
                        }
                        if (uuid != null) line = line.substring(line.indexOf('>', semiColonIndex + 1) + 1);
                    }
                    return SignedComponent.sign(uuid, line, serializerFor(player));
                })
                .toList();
            item.setData(DataComponentTypes.LORE, ItemLore.lore().lines(lore).build());
        }
    }
}
