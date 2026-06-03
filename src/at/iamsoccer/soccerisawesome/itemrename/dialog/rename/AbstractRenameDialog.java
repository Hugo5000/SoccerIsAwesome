package at.iamsoccer.soccerisawesome.itemrename.dialog.rename;

import at.hugob.plugin.library.config.ConfigUtils;
import at.hugob.plugin.library.config.MiniMsgLegacyHybridSerializer;
import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.DecorationResolvers;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractPreviewAndApplyEditorDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.IDialogFactory;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.Nullable;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractRenameDialog extends AbstractPreviewAndApplyEditorDialog {
    public static final ClickCallback.Options UNLIMITED_CALLBACK_OPTIONS = ClickCallback.Options.builder().uses(-1).lifetime(ChronoUnit.FOREVER.getDuration()).build();
    public static final NamespacedKey rawDataKey = new NamespacedKey("rename", "raw");
    public static final NamespacedKey plainDataKey = new NamespacedKey("rename", "plain");
    public final NamespacedKey pdcDataKey;

    private Component differenceWarning = Component.empty();
    // Input Labels
    private Component label = Component.empty();
    // Button Labels

    public AbstractRenameDialog(NamespacedKey pdcDataKey, Permission permission, @Nullable Supplier<IDialogFactory> returnDialogSupplier) {
        super(permission, returnDialogSupplier);
        this.pdcDataKey = pdcDataKey;
    }

    protected record SuggestionResult(
        String suggestion,
        boolean isDifferent
    ) {
    }

    @Override
    public void reload(YamlFileConfig configFile, ConfigurationSection configSection) {
        super.reload(configFile, configSection);
        differenceWarning = ConfigUtils.parseComponent(configFile, configSection.isString("difference-warning")
                ? configSection.getString("difference-warning")
                : configSection.getString("dialog.default.difference-warning")
            , null, null);
        label = ConfigUtils.parseComponent(configFile, configSection.isString("label")
                ? configSection.getString("label")
                : ""
            , null, null);
    }

    protected abstract SuggestionResult getSuggestionFromItem(Player player, ItemStack item);
    protected abstract boolean isDifferentThanExpected(Player player, ItemStack item);

    @Override
    protected List<DialogBody> body(List<DialogBody> body, Player player, @Nullable DialogResponseView responseView) {
        var item = player.getInventory().getItemInMainHand().clone();
        applyToPreviewItem(player, getValue(responseView, "name", getSuggestionFromItem(player, item).suggestion), item);
        if (responseView == null && isDifferentThanExpected(player, item))
            body.add(DialogBody.plainMessage(differenceWarning));
        body.add(DialogBody.item(item).build());
        return body;
    }

    @Override
    protected List<DialogInput> inputs(Player player, @Nullable DialogResponseView responseView) {
        var item = player.getInventory().getItemInMainHand();
        return List.of(
            DialogInput.text("name", label)
                .maxLength(16000)
                .initial(getValue(responseView, "name", getValue(responseView, "name", getSuggestionFromItem(player, item).suggestion)))
                .multiline(TextDialogInput.MultilineOptions.create(null, 100))
                .build()
        );
    }

    protected Component parseIntoPreviewComponent(Player player, String text) {
        return parseLine(player, text);
    }

    protected static String parseComponent(Component component) {
        return MiniMsgLegacyHybridSerializer.INSTANCE.serialize(component.compact(COMPACT_STYLE));
    }

    @Override
    protected void onApply(DialogResponseView response, Player player) {
        var item = player.getInventory().getItemInMainHand();
        String input = getValue(response, "name", "");
        // TODO: limit length
        applyToItem(player, input, item);
        item.editPersistentDataContainer(pdc -> applyToPDC(player, pdc, input));
    }

    protected abstract void applyToItem(Player player, String input, ItemStack item);

    protected void applyToPreviewItem(Player player, String input, ItemStack item) {
        applyToItem(player, input, item);
    }

    protected abstract void applyToPDC(Player player, PersistentDataContainer pdc, String input);

    protected Component parseLine(Player player, String input) {
        return serializerFor(player).deserialize(input)
            .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
            .colorIfAbsent(NamedTextColor.WHITE);
    }


    @Override
    protected void onPreview(DialogResponseView response, Player player) {
        player.showDialog(create(player, response));
    }

    // <bold><#392216>C<#3E2719>h<#432B1C>o<#48301F>c<#4C3422>o<#513925>l<#563E29>a<#5B422C>t<#60472F>e <#695035>E<#6E5438>g<#73593B>g
    // {bold:1b, extra: [{text:"test",color:"#392216"}]}
    // {"bold":true, extra: [{"color":"#392216","text":"C"},{"color":"#3E2719","text":"h"},{"color":"#432B1C","text":"o"},{"color":"#48301F","text":"c"},{"color":"#4C3422","text":"o"},{"color":"#513925","text":"l"},{"color":"#563E29","text":"a"},{"color":"#5B422C","text":"t"},{"color":"#60472F","text":"e "},{"color":"#695035","text":"E"},{"color":"#6E5438","text":"g"},{"color":"#73593B","text":"g"}],"text":""}
    // {"bold":true,"color":"#392216","extra":[{"color":"#3E2719","extra":[{"color":"#432B1C","extra":[{"color":"#48301F","extra":[{"color":"#4C3422","extra":[{"color":"#513925","extra":[{"color":"#563E29","extra":[{"color":"#5B422C","extra":[{"color":"#60472F","extra":[{"color":"#695035","extra":[{"color":"#6E5438","extra":[{"color":"#73593B","text":"g"}],"text":"g"}],"text":"E"}],"text":"e "}],"text":"t"}],"text":"a"}],"text":"l"}],"text":"o"}],"text":"c"}],"text":"o"}],"text":"h"}],"text":"C"}

    private static final Style COMPACT_STYLE = Style.style()
        .color(NamedTextColor.WHITE)
        .decoration(TextDecoration.ITALIC, false)
        .decoration(TextDecoration.OBFUSCATED, false)
        .decoration(TextDecoration.BOLD, false)
        .decoration(TextDecoration.STRIKETHROUGH, false)
        .decoration(TextDecoration.UNDERLINED, false)
        .build();

    private static MiniMessage serializerFor(Player player) {
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
