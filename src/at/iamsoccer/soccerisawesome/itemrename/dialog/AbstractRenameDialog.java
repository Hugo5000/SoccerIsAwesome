package at.iamsoccer.soccerisawesome.itemrename.dialog;

import at.hugob.plugin.library.config.MiniMsgLegacyHybridSerializer;
import at.iamsoccer.soccerisawesome.DecorationResolvers;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

public abstract class AbstractRenameDialog {
    public static final ClickCallback.Options UNLIMITED_CALLBACK_OPTIONS = ClickCallback.Options.builder().uses(-1).lifetime(ChronoUnit.FOREVER.getDuration()).build();
    public final NamespacedKey pdcDataKey;

    private List<Component> info = Collections.emptyList();
    private Component label = Component.empty();

    public AbstractRenameDialog(NamespacedKey pdcDataKey) {
        this.pdcDataKey = pdcDataKey;
    }

    private final DialogAction applyAction = DialogAction.customClick(this::onApply, UNLIMITED_CALLBACK_OPTIONS);
    private final DialogAction previewAction = DialogAction.customClick(this::onPreview, UNLIMITED_CALLBACK_OPTIONS);

    public DialogLike create(Player player) {
        var item = player.getInventory().getItemInMainHand();

        final String suggestion = getSuggestionFromItem(item);

        // TODO: flatten the suggestion
        return getDialog(player, item, suggestion);
    }

    protected abstract String getSuggestionFromItem(ItemStack item);

    private @NonNull Dialog getDialog(Player player, ItemStack itemStack, String text) {
        var name = parseIntoPreviewComponent(player, text);
        var item = itemStack.clone();
        applyToItem(player, text, item);
        return Dialog.create(builder ->
            builder.empty().base(DialogBase.builder(Component.text("Item Rename"))
                    .body(List.of(
                        DialogBody.plainMessage(name),
                        DialogBody.item(item).build()
                    ))
                    .inputs(List.of(
                        DialogInput.text("name", Component.text("Item Name"))
                            .maxLength(16000)
                            .initial(text)
                            .multiline(TextDialogInput.MultilineOptions.create(null, 150))
                            .build()
                    ))
                    .canCloseWithEscape(true)
                    .build())
                .type(DialogType.multiAction(List.of(
                    ActionButton.builder(Component.text("Preview Name")).action(previewAction).build(),
                    ActionButton.builder(Component.text("Apply")).action(applyAction).build()
                ), ActionButton.builder(Component.text("Cancel")).build(), 1))
        );
    }

    protected @NonNull Component parseIntoPreviewComponent(Player player, String text) {
        return parseLine(player, text);
    }

    protected static @NonNull String parseComponent(Component component) {
        return MiniMsgLegacyHybridSerializer.INSTANCE.serialize(component.compact(COMPACT_STYLE));
    }

    private void onApply(DialogResponseView response, Audience audience) {
        if (!(audience instanceof Player player)) return;
        var item = player.getInventory().getItemInMainHand();
        String input = response.getText("name");
        // TODO: limit length
        applyToItem(player, input, item);
        item.editPersistentDataContainer(pdc -> applyToPDC(pdc, input));
    }

    protected abstract void applyToItem(Player player, String input, ItemStack item);

    protected abstract void applyToPDC(PersistentDataContainer pdc, String input);

    protected @NonNull Component parseLine(Player player, String input) {
        return serializerFor(player).deserialize(input)
            .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
            .colorIfAbsent(NamedTextColor.WHITE);
    }

    private void onPreview(DialogResponseView response, Audience audience) {
        if (!(audience instanceof Player player)) return;
        audience.showDialog(getDialog(player, player.getInventory().getItemInMainHand(), response.getText("name")));
    }

    // <bold><#392216>C<#3E2719>h<#432B1C>o<#48301F>c<#4C3422>o<#513925>l<#563E29>a<#5B422C>t<#60472F>e <#695035>E<#6E5438>g<#73593B>g
    // {bold:1b, extra: [{text:"test",color:"#392216"}]}
    // {"bold":true, extra: [{"color":"#392216","text":"C"},{"color":"#3E2719","text":"h"},{"color":"#432B1C","text":"o"},{"color":"#48301F","text":"c"},{"color":"#4C3422","text":"o"},{"color":"#513925","text":"l"},{"color":"#563E29","text":"a"},{"color":"#5B422C","text":"t"},{"color":"#60472F","text":"e "},{"color":"#695035","text":"E"},{"color":"#6E5438","text":"g"},{"color":"#73593B","text":"g"}],"text":""}
    // {"bold":true,"color":"#392216","extra":[{"color":"#3E2719","extra":[{"color":"#432B1C","extra":[{"color":"#48301F","extra":[{"color":"#4C3422","extra":[{"color":"#513925","extra":[{"color":"#563E29","extra":[{"color":"#5B422C","extra":[{"color":"#60472F","extra":[{"color":"#695035","extra":[{"color":"#6E5438","extra":[{"color":"#73593B","text":"g"}],"text":"g"}],"text":"E"}],"text":"e "}],"text":"t"}],"text":"a"}],"text":"l"}],"text":"o"}],"text":"c"}],"text":"o"}],"text":"h"}],"text":"C"}

    // TODO: make private
    static final @NotNull Style COMPACT_STYLE = Style.style()
        .color(NamedTextColor.WHITE)
        .decoration(TextDecoration.ITALIC, false)
        .decoration(TextDecoration.OBFUSCATED, false)
        .decoration(TextDecoration.BOLD, false)
        .decoration(TextDecoration.STRIKETHROUGH, false)
        .decoration(TextDecoration.UNDERLINED, false)
        .build();

    // TODO: make private
    static @NotNull MiniMessage serializerFor(Player player) {
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
