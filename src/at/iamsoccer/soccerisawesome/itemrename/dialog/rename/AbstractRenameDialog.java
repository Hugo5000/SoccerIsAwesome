package at.iamsoccer.soccerisawesome.itemrename.dialog.rename;

import at.hugob.plugin.library.config.ConfigUtils;
import at.hugob.plugin.library.config.MiniMsgLegacyHybridSerializer;
import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.DecorationResolvers;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractItemPreviewAndApplyDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.AbstractDialogFactory;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.Nullable;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractRenameDialog extends AbstractItemPreviewAndApplyDialog {
    public static final ClickCallback.Options UNLIMITED_CALLBACK_OPTIONS = ClickCallback.Options.builder().uses(-1).lifetime(ChronoUnit.FOREVER.getDuration()).build();

    private Component differenceWarning = Component.empty();
    // Input Labels
    private Component label = Component.empty();
    // Button Labels

    public AbstractRenameDialog(@Nullable Permission permission, @Nullable Supplier<AbstractDialogFactory<Player>> returnDialogSupplier) {
        super(permission, returnDialogSupplier);
    }

    @Override
    public void reload(YamlFileConfig configFile, ConfigurationSection configSection) {
        super.reload(configFile, configSection);
        differenceWarning = ConfigUtils.parseComponent(configFile, Objects.requireNonNullElse(
            configSection.isString("difference-warning")
                ? configSection.getString("difference-warning")
                : configFile.getString("dialog.default.difference-warning"),
            configSection.getCurrentPath() + ".difference-warning"
        ), null, null);
        label = ConfigUtils.parseComponent(configFile, Objects.requireNonNullElse(
            configSection.getString("label"),
            configSection.getCurrentPath() + ".label"
        ), null, null);
    }

    protected abstract SuggestionResult getSuggestionFromItem(Player player, ItemStack item);
    protected abstract boolean isDifferentThanExpected(Player player, ItemStack item);

    @Override
    protected List<DialogBody> dialogBody(Player player, @Nullable DialogResponseView response) {
        var body = super.dialogBody(player, response);
        var item = player.getInventory().getItemInMainHand().clone();
        if (response == null && isDifferentThanExpected(player, item))
            body.add(DialogBody.plainMessage(differenceWarning));
        return body;
    }

    @Override
    protected List<DialogInput> dialogInputs(Player player, @Nullable DialogResponseView responseView) {
        var item = player.getInventory().getItemInMainHand();
        return List.of(
            DialogInput.text("name", label)
                .maxLength(16000)
                .initial(getString(responseView, "name", () -> getSuggestionFromItem(player, item).suggestion))
                .multiline(TextDialogInput.MultilineOptions.create(null, 150))
                .width(300)
                .build()
        );
    }

    protected Component parseIntoPreviewComponent(Player player, String text) {
        return serializerFor(player).deserialize(text);
    }

    @Override
    protected void applyToItem(Player player, DialogResponseView response, ItemStack item) {
        String input = getString(response, "name", () -> "");
        // TODO: limit length
        applyToItem(player, input, item);
    }

    protected abstract void applyToItem(Player player, String input, ItemStack item);

    @Override
    protected void modifyPreview(Player player, @Nullable DialogResponseView response, ItemStack item) {
        if (response == null) return;
        applyToItem(player, getString(response, "name", () -> getSuggestionFromItem(player, item).suggestion), item);
    }

    public record SuggestionResult(
        String suggestion,
        boolean isDifferent
    ) {
    }

    // <bold><#392216>C<#3E2719>h<#432B1C>o<#48301F>c<#4C3422>o<#513925>l<#563E29>a<#5B422C>t<#60472F>e <#695035>E<#6E5438>g<#73593B>g
    // {bold:1b, extra: [{text:"test",color:"#392216"}]}
    // {"bold":true, extra: [{"color":"#392216","text":"C"},{"color":"#3E2719","text":"h"},{"color":"#432B1C","text":"o"},{"color":"#48301F","text":"c"},{"color":"#4C3422","text":"o"},{"color":"#513925","text":"l"},{"color":"#563E29","text":"a"},{"color":"#5B422C","text":"t"},{"color":"#60472F","text":"e "},{"color":"#695035","text":"E"},{"color":"#6E5438","text":"g"},{"color":"#73593B","text":"g"}],"text":""}
    // {"bold":true,"color":"#392216","extra":[{"color":"#3E2719","extra":[{"color":"#432B1C","extra":[{"color":"#48301F","extra":[{"color":"#4C3422","extra":[{"color":"#513925","extra":[{"color":"#563E29","extra":[{"color":"#5B422C","extra":[{"color":"#60472F","extra":[{"color":"#695035","extra":[{"color":"#6E5438","extra":[{"color":"#73593B","text":"g"}],"text":"g"}],"text":"E"}],"text":"e "}],"text":"t"}],"text":"a"}],"text":"l"}],"text":"o"}],"text":"c"}],"text":"o"}],"text":"h"}],"text":"C"}

    protected static final Style COMPACT_STYLE = Style.style()
        .color(NamedTextColor.WHITE)
        .decoration(TextDecoration.ITALIC, false)
        .decoration(TextDecoration.OBFUSCATED, false)
        .decoration(TextDecoration.BOLD, false)
        .decoration(TextDecoration.STRIKETHROUGH, false)
        .decoration(TextDecoration.UNDERLINED, false)
        .build();

    protected static MiniMessage serializerFor(Player player) {
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
