package at.iamsoccer.soccerisawesome.itemrename;

import at.hugob.plugin.library.config.MiniMsgLegacyHybridSerializer;
import at.iamsoccer.soccerisawesome.AbstractModule;
import at.iamsoccer.soccerisawesome.DecorationResolvers;
import at.iamsoccer.soccerisawesome.SoccerIsAwesomePlugin;
import at.iamsoccer.soccerisawesome.itemrename.dialog.ItemCustomNameRenameDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.ItemLoreRenameDialog;
import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemRenameModule extends AbstractModule implements Listener {
    public ItemRenameModule(SoccerIsAwesomePlugin plugin) {
        super(plugin, "ItemRename");
    }

    @Override
    public void lifecycleHandler(SoccerIsAwesomePlugin.ICommandRegistration register) {
        register.register(Commands.literal("rename")
                .requires(css -> css.getSender() instanceof Player player && player.hasPermission("shia.rename.custom-name.command"))
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    player.showDialog(ItemCustomNameRenameDialog.create(player));
                    return Command.SINGLE_SUCCESS;
                })
                .build(),
            "Allows renaming of an Items Name",
            List.of("setname")
        );
        register.register(Commands.literal("relore")
                .requires(css -> css.getSender() instanceof Player player && player.hasPermission("shia.rename.lore"))
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    player.showDialog(ItemLoreRenameDialog.create(player));
                    return Command.SINGLE_SUCCESS;
                })
                .build(),
            "Allows changing an Items Lore",
            List.of("setlore")
        );
    }

    public static final @NotNull Style COMPACT_STYLE = Style.style()
        .color(NamedTextColor.WHITE)
        .decoration(TextDecoration.ITALIC, false)
        .decoration(TextDecoration.OBFUSCATED, false)
        .decoration(TextDecoration.BOLD, false)
        .decoration(TextDecoration.STRIKETHROUGH, false)
        .decoration(TextDecoration.UNDERLINED, false)
        .build();

    public static @NotNull MiniMessage serializerFor(Player player) {
        var builder = MiniMessage.builder();
        var resolver = TagResolver.builder();

        resolver.resolver(StandardTags.reset());
        resolver.resolver(StandardTags.translatable());
        if (player.hasPermission("shia.rename.format.color")) {
            resolver.resolver(StandardTags.color());
            resolver.resolver(StandardTags.gradient());
            resolver.resolver(StandardTags.rainbow());
            resolver.resolver(StandardTags.pride());
            builder.preProcessor(MiniMsgLegacyHybridSerializer::parseLegacy);
        }
        if (player.hasPermission("shia.rename.format.font")) {
            resolver.resolver(StandardTags.font());
        }
        if (player.hasPermission("shia.rename.format.shadow")) {
            resolver.resolver(StandardTags.shadowColor());
        }
        if (player.hasPermission("shia.rename.format.sprites")) {
            resolver.resolver(StandardTags.sprite());
        }
        if (player.hasPermission("shia.rename.format.keybind")) {
            resolver.resolver(StandardTags.keybind());
        }
        if (player.hasPermission("shia.rename.format.italic")) {
            resolver.resolver(DecorationResolvers.ITALIC);
        }
        if (player.hasPermission("shia.rename.format.bold")) {
            resolver.resolver(DecorationResolvers.BOLD);
        }
        if (player.hasPermission("shia.rename.format.obfuscated")) {
            resolver.resolver(DecorationResolvers.OBFUSCATED);
        }
        if (player.hasPermission("shia.rename.format.strikethrough")) {
            resolver.resolver(DecorationResolvers.STRIKETHROUGH);
        }
        if (player.hasPermission("shia.rename.format.underlined")) {
            resolver.resolver(DecorationResolvers.UNDERLINED);
        }
        builder.tags(resolver.build());
        builder.postProcessor(c -> c.compact(COMPACT_STYLE));
        return builder.build();
    }

}
