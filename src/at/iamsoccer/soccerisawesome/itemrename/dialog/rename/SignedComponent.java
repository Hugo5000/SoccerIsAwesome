package at.iamsoccer.soccerisawesome.itemrename.dialog.rename;

import at.hugob.plugin.library.config.MiniMsgLegacyHybridSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.UUID;

public record SignedComponent(
    Component component,
    @Nullable UUID signeeUUID,
    String rawText, int plainHash
) {
    private final static String key = "owner";

    public static SignedComponent parse(Component component) {
        if (!(component instanceof TranslatableComponent translatable)) return unSigned(component);
        if (!translatable.key().equals(key)) return unSigned(component);
        var args = translatable.arguments();
        if (args.size() == 3) {
            @Nullable var uuid = args.get(0).value() instanceof TextComponent txt ? UUID.fromString(txt.content()) : null;
            return parsePlainAndRawArgs(translatable, args, uuid, 1);
        }
        if (args.size() == 2) {
            return parsePlainAndRawArgs(translatable, args, null, 0);
        }
        return unSigned(component);
    }

    private static @NonNull SignedComponent parsePlainAndRawArgs(TranslatableComponent translatable, List<TranslationArgument> args, @Nullable UUID uuid, int startIndex) {
        @Nullable var raw = args.get(startIndex++).value() instanceof TextComponent txt ? txt.content() : null;
        @Nullable Integer plain = null;
        try {
            plain = args.get(startIndex++).value() instanceof TextComponent txt ? Integer.parseInt(txt.content()) : null;
        } catch (NumberFormatException e) {
        }
        if (plain == null || raw == null) return unSigned(translatable);
        return new SignedComponent(
            translatable.children().size() == 1 ? translatable.children().getFirst() : Component.textOfChildren(Component.textOfChildren(translatable.children().toArray(ComponentLike[]::new))),
            uuid, raw, plain
        );
    }

    public static TranslatableComponent sign(Player player, String rawText, MiniMessage serializer) {
        return sign(player.getUniqueId(), rawText, serializer);
    }

    public static TranslatableComponent sign(@Nullable UUID uuid, String rawText, MiniMessage serializer) {
        if (uuid == null) return unSigned(rawText, serializer);
        var translatedComponent = serializer.deserialize(rawText);
        return Component.translatable()
            .key(key)
            .arguments(
                Component.text(uuid.toString()),
                Component.text(rawText),
                Component.text(PlainTextComponentSerializer.plainText().serialize(translatedComponent).hashCode())
            ).fallback("")
            .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
            .colorIfAbsent(NamedTextColor.WHITE)
            .append(translatedComponent)
            .build();
    }

    public static TranslatableComponent unSigned(String rawText, MiniMessage serializer) {
        var translatedComponent = serializer.deserialize(rawText);
        return Component.translatable()
            .key(key)
            .arguments(
                Component.text(rawText),
                Component.text(PlainTextComponentSerializer.plainText().serialize(translatedComponent).hashCode())
            ).fallback("")
            .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
            .colorIfAbsent(NamedTextColor.WHITE)
            .append(translatedComponent)
            .build();
    }

    public static SignedComponent unSigned(Component component) {
        var raw = MiniMsgLegacyHybridSerializer.INSTANCE.serialize(component.compact(AbstractRenameDialog.COMPACT_STYLE));
        var plain = PlainTextComponentSerializer.plainText().serialize(component).hashCode();
        return new SignedComponent(component, null, raw, plain);
    }

    public boolean isSigned() {
        return signeeUUID != null;
    }

    public @Nullable String signeeName() {
        if (signeeUUID == null) return null;
        return Bukkit.getOfflinePlayer(signeeUUID).getName();
    }
}
