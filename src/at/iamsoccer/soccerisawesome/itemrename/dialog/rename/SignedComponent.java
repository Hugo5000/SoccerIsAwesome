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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public abstract class SignedComponent {
    private final static UUID SERVER_UUID = new UUID(0, 0);
    private final static String key = "owner";

    private final Component component;
    private final String rawText;
    private final int plainHash;

    protected SignedComponent(Component component, String rawText, int plainHash) {
        this.component = component;
        this.rawText = rawText;
        this.plainHash = plainHash;
    }

    protected SignedComponent(Component component, String rawText) {
        this.component = component;
        this.rawText = rawText;
        this.plainHash = PlainTextComponentSerializer.plainText().serialize(component).hashCode();
    }

    protected SignedComponent(Component component) {
        this.component = component;
        this.rawText = MiniMsgLegacyHybridSerializer.INSTANCE.serialize(component.compact(AbstractRenameDialog.COMPACT_STYLE));
        this.plainHash = PlainTextComponentSerializer.plainText().serialize(component).hashCode();
    }

    public Component component() {
        return component;
    }

    public String rawText() {
        return rawText;
    }

    public int plainHash() {
        return plainHash;
    }

    public boolean isPlayerSigned() {
        return false;
    }

    public boolean isUnknown() {
        return false;
    }

    public boolean isServerSigned() {
        return false;
    }

    public @Nullable UUID signeeUUID() {
        return null;
    }

    public @Nullable String signeeName() {
        return null;
    }

    public boolean isUnsigned() {
        return false;
    }

    public static SignedComponent parse(Component component) {
        // default all components that weren't created with this plugin as server signed
        if (!(component instanceof TranslatableComponent translatable)) return new Unknown(component);
        if (!translatable.key().equals(key)) return new Unknown(component);
        var args = translatable.arguments();
        if (args.size() == 3) {
            // is player signed
            @Nullable var uuid = args.get(0).value() instanceof TextComponent txt ? txt.content().isBlank() ? SERVER_UUID : UUID.fromString(txt.content()) : null;
            return parsePlainAndRawArgs(translatable, args, uuid, 1);
        }
        if (args.size() == 2) {
            // is unsigned
            return parsePlainAndRawArgs(translatable, args, null, 0);
        }
        return new Unknown(component);
    }

    private static SignedComponent parsePlainAndRawArgs(TranslatableComponent translatable, List<TranslationArgument> args, @Nullable UUID uuid, int startIndex) {
        @Nullable var raw = args.get(startIndex++).value() instanceof TextComponent txt ? txt.content() : null;
        @Nullable Integer plain = getInteger(args.get(startIndex++).value());
        if (plain == null || raw == null) return new Unknown(translatable);
        var comp = translatable.children().size() == 1 ? translatable.children().getFirst() : Component.textOfChildren(Component.textOfChildren(translatable.children().toArray(ComponentLike[]::new)));
        if (uuid == null) return new SignedComponent.Unsigned(comp, raw, plain);
        if (SERVER_UUID.equals(uuid)) return new SignedComponent.Server(comp, raw, plain);
        return new SignedComponent.Player(comp, raw, plain, uuid);
    }

    private static @Nullable Integer getInteger(Object value) {
        try {
            return value instanceof TextComponent txt ? Integer.parseInt(txt.content()) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static SignedComponent signServer(String rawText, MiniMessage serializer) {
        return new Server(serializer.deserialize(rawText), rawText);
    }

    public static SignedComponent sign(UUID uuid, String rawText, MiniMessage serializer) {
        var translatedComponent = serializer.deserialize(rawText);
        return new Player(translatedComponent, rawText, uuid);
    }

    public static SignedComponent unSigned(String rawText, MiniMessage serializer) {
        var translatedComponent = serializer.deserialize(rawText);
        return new Unsigned(translatedComponent, rawText);
    }

    public static class Player extends SignedComponent {
        private final UUID signeeUUID;

        protected Player(Component component, String rawText, int plainHash, UUID signeeUUID) {
            super(component, rawText, plainHash);
            this.signeeUUID = signeeUUID;
        }

        protected Player(Component component, String rawText, UUID signeeUUID) {
            super(component, rawText);
            this.signeeUUID = signeeUUID;
        }


        protected Player(Component component, UUID signeeUUID) {
            super(component);
            this.signeeUUID = signeeUUID;
        }

        @Override
        public TranslatableComponent component() {
            return Component.translatable()
                .key(key)
                .arguments(
                    Component.text(signeeUUID.toString()),
                    Component.text(rawText()),
                    Component.text(plainHash())
                ).fallback("")
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .colorIfAbsent(NamedTextColor.WHITE)
                .append(super.component())
                .build();
        }

        @Override
        public boolean isPlayerSigned() {
            return true;
        }

        @Override
        public @Nullable UUID signeeUUID() {
            return signeeUUID;
        }

        @Override
        public @Nullable String signeeName() {
            return Bukkit.getOfflinePlayer(signeeUUID).getName();
        }

    }

    public static class Server extends SignedComponent {
        protected Server(Component component, String rawText, int plainHash) {
            super(component, rawText, plainHash);
        }

        protected Server(Component component, String rawText) {
            super(component, rawText);
        }

        protected Server(Component component) {
            super(component);
        }

        @Override
        public TranslatableComponent component() {
            return Component.translatable()
                .key(key)
                .arguments(
                    Component.text(""),
                    Component.text(rawText()),
                    Component.text(plainHash())
                ).fallback("")
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .colorIfAbsent(NamedTextColor.WHITE)
                .append(super.component())
                .build();
        }

        @Override
        public boolean isServerSigned() {
            return true;
        }
    }

    public static class Unknown extends SignedComponent {
        protected Unknown(Component component, String rawText, int plainHash) {
            super(component, rawText, plainHash);
        }

        protected Unknown(Component component, String rawText) {
            super(component, rawText);
        }

        protected Unknown(Component component) {
            super(component);
        }

        @Override
        public boolean isUnknown() {
            return true;
        }
    }

    public static class Unsigned extends SignedComponent {
        protected Unsigned(Component component, String rawText, int plainHash) {
            super(component, rawText, plainHash);
        }

        protected Unsigned(Component component, String rawText) {
            super(component, rawText);
        }

        protected Unsigned(Component component) {
            super(component);
        }

        @Override
        public boolean isUnsigned() {
            return true;
        }

        @Override
        public TranslatableComponent component() {
            return Component.translatable()
                .key(key)
                .arguments(
                    Component.text(rawText()),
                    Component.text(plainHash())
                ).fallback("")
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .colorIfAbsent(NamedTextColor.WHITE)
                .append(super.component())
                .build();
        }
    }
}
