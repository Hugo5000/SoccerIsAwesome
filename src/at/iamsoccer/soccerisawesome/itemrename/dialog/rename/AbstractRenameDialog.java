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
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.Nullable;

import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractRenameDialog extends AbstractItemPreviewAndApplyDialog {
    public static final ClickCallback.Options UNLIMITED_CALLBACK_OPTIONS = ClickCallback.Options.builder().uses(-1).lifetime(ChronoUnit.FOREVER.getDuration()).build();

    private Component differenceWarning = Component.empty();
    // Input Labels
    private Component label = Component.empty();

    private int characterLimit = 1000;
    private int lineLengthLimit = 50;
    private int maxLinesLimit = 10;

    private String validationTooLong = "";
    private String validationTooManyLines = "";

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
        validationTooLong = Objects.requireNonNullElse(
            configSection.isString("validation.too-long")
                ? configSection.getString("validation.too-long")
                : configFile.getString("dialog.default.validation.too-long"),
            configSection.getCurrentPath() + ".validation.too-long"
        );
        validationTooManyLines = Objects.requireNonNullElse(
            configSection.isString("validation.too-many-lines")
                ? configSection.getString("validation.too-many-lines")
                : configFile.getString("dialog.default.validation.too-many-lines"),
            configSection.getCurrentPath() + ".validation.too-many-lines"
        );
        characterLimit = configSection.isInt("validation.max-characters")
            ? configSection.getInt("validation.max-characters")
            : configFile.getInt("dialog.default.validation.max-characters");
        if (characterLimit == 0) characterLimit = 1000;
        else if (characterLimit < 0) characterLimit = 16000;
        lineLengthLimit = configSection.isInt("validation.max-line-length")
            ? configSection.getInt("validation.max-line-length")
            : configFile.getInt("dialog.default.validation.max-line-length");
        if (lineLengthLimit == 0) lineLengthLimit = 50;
        else if (lineLengthLimit < 0) lineLengthLimit = Integer.MAX_VALUE;
        maxLinesLimit = configSection.isInt("validation.max-lines")
            ? configSection.getInt("validation.max-lines")
            : configFile.getInt("dialog.default.validation.max-lines");
        if (maxLinesLimit == 0) maxLinesLimit = 10;
        else if (maxLinesLimit < 0) maxLinesLimit = Integer.MAX_VALUE;
    }

    protected abstract SuggestionResult getSuggestionFromItem(Player player, ItemStack item);
    protected abstract boolean isDifferentThanExpected(Player player, ItemStack item);

    @Override
    protected List<DialogBody> dialogBody(Player player, @Nullable DialogResponseView response) {
        var body = super.dialogBody(player, response);
        var item = player.getInventory().getItemInMainHand().clone();
        if (response == null && isDifferentThanExpected(player, item))
            body.add(DialogBody.plainMessage(differenceWarning));
        if (response != null) {
            String input = getString(response, "name", () -> "");
            var validationResult = validateInput(player, input);
            for (ValidationResult result : validationResult) {
                var length = PlainTextComponentSerializer.plainText().serialize(serializerFor(player, true).deserialize(input)).length();
                TagResolver culpritResolver = TagResolver.builder()
                    .tag("culprit", Tag.selfClosingInserting(result.culprit))
                    .tag("max_length", Tag.preProcessParsed(String.valueOf(lineLengthLimit)))
                    .tag("length", Tag.preProcessParsed(String.valueOf(length)))
                    .tag("length_difference", Tag.preProcessParsed(String.valueOf(length - lineLengthLimit)))
                    .tag("max_lines", Tag.preProcessParsed(String.valueOf(maxLinesLimit)))
                    .tag("lines", Tag.preProcessParsed(String.valueOf(input.lines().count())))
                    .tag("lines_difference", Tag.preProcessParsed(String.valueOf(input.lines().count() - maxLinesLimit)))
                    .build();
                var string = switch (result.reason) {
                    case TOO_LONG -> validationTooLong;
                    case TOO_MANY_LINES -> validationTooManyLines;
                };
                body.add(DialogBody.plainMessage(ConfigUtils.parseComponent(config, string, culpritResolver, null)));
            }
        }
        return body;
    }

    @Override
    protected List<DialogInput> dialogInputs(Player player, @Nullable DialogResponseView responseView) {
        var item = player.getInventory().getItemInMainHand();
        var suggestion = getString(responseView, "name", () -> getSuggestionFromItem(player, item).suggestion);
        return List.of(
            DialogInput.text("name", label)
                .maxLength(Math.max(characterLimit, suggestion.length()))
                .initial(suggestion)
                .multiline(TextDialogInput.MultilineOptions.create(null, Math.max(150, Math.min(512, (int) suggestion.lines().count() * 10 + 30))))
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
        applyToItem(player, input, item);
    }

    @Override
    protected boolean quickValidateInput(Player player, DialogResponseView response) {
        return quickValidateInput(player, getString(response, "name", () -> ""));
    }

    protected boolean quickValidateInput(Player player, String input) {
        var comp = serializerFor(player, true).deserialize(input);
        return PlainTextComponentSerializer.plainText().serialize(comp).length() <= lineLengthLimit;
    }

    protected List<ValidationResult> validateInput(Player player, String input) {
        var comp = serializerFor(player, true).deserialize(input);
        if (PlainTextComponentSerializer.plainText().serialize(comp).length() > lineLengthLimit) {
            return List.of(
                new ValidationResult(
                    comp, ValidationReason.TOO_LONG
                )
            );
        }
        return Collections.emptyList();
    }

    public record ValidationResult(
        Component culprit, ValidationReason reason
    ) {
    }

    protected enum ValidationReason {
        TOO_LONG,
        TOO_MANY_LINES
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

    protected int characterLimit() {
        return characterLimit;
    }

    protected int lineLengthLimit() {
        return lineLengthLimit;
    }

    protected int maxLinesLimit() {
        return maxLinesLimit;
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
        return serializerFor(player, false);
    }

    protected static MiniMessage serializerFor(Player player, boolean simple) {
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
            if (simple) {
                resolver.tag("sprite", (argumentQueue, context) -> Tag.preProcessParsed("#"));
            } else {
                resolver.resolver(StandardTags.sprite());
            }
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
