package at.iamsoccer.soccerisawesome.itemrename.dialog;

import at.hugob.plugin.library.config.MiniMsgLegacyHybridSerializer;
import io.papermc.paper.datacomponent.DataComponentTypes;
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
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import java.time.temporal.ChronoUnit;
import java.util.List;

public class ItemRenameDialog {
    public static final ClickCallback.Options UNLIMITED_CALLBACK_OPTIONS = ClickCallback.Options.builder().uses(-1).lifetime(ChronoUnit.FOREVER.getDuration()).build();

    private ItemRenameDialog() {
    }

    private static final DialogAction applyAction = DialogAction.customClick(ItemRenameDialog::onApply, UNLIMITED_CALLBACK_OPTIONS);
    private static final DialogAction previewAction = DialogAction.customClick(ItemRenameDialog::onPreview, UNLIMITED_CALLBACK_OPTIONS);

    public static DialogLike create(Player player) {
        var item = player.getInventory().getItemInMainHand();

        final String text;
        if (item.hasData(DataComponentTypes.CUSTOM_NAME)) {
            text = parseComponent(item.getData(DataComponentTypes.CUSTOM_NAME));
        } else if (item.hasData(DataComponentTypes.ITEM_NAME)) {
            text = parseComponent(item.getData(DataComponentTypes.ITEM_NAME));
        } else {
            text = parseComponent(item.effectiveName());
        }
        // TODO: flatten the suggestion, or load the last rename suggestion
        return getDialog(item, text);
    }

    private static @NonNull Dialog getDialog(ItemStack itemStack, String text) {
        MiniMessage instance = MiniMsgLegacyHybridSerializer.INSTANCE;
        var name = instance.deserialize(text);
        var item = itemStack.clone();
        item.setData(DataComponentTypes.CUSTOM_NAME, Component.empty().decoration(TextDecoration.ITALIC,false).append(name));
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

    private static @NonNull String parseComponent(Component component) {
        return MiniMsgLegacyHybridSerializer.INSTANCE.serialize(component.compact(Style.style()
            .color(NamedTextColor.WHITE)
            .decoration(TextDecoration.ITALIC, false)
            .decoration(TextDecoration.OBFUSCATED, false)
            .decoration(TextDecoration.BOLD, false)
            .decoration(TextDecoration.STRIKETHROUGH, false)
            .decoration(TextDecoration.UNDERLINED, false)
            .build()));
    }

    private static void onApply(DialogResponseView response, Audience audience) {
        if(!(audience instanceof Player player)) return;
        player.getInventory().getItemInMainHand().setData(DataComponentTypes.CUSTOM_NAME, Component.empty()
            .decoration(TextDecoration.ITALIC,false)
            .append(MiniMsgLegacyHybridSerializer.INSTANCE.deserialize(response.getText("name")))
        );
    }

    private static void onPreview(DialogResponseView response, Audience audience) {
        if(!(audience instanceof Player player)) return;
        audience.showDialog(getDialog(player.getInventory().getItemInMainHand(), response.getText("name")));
    }

    // <bold><#392216>C<#3E2719>h<#432B1C>o<#48301F>c<#4C3422>o<#513925>l<#563E29>a<#5B422C>t<#60472F>e <#695035>E<#6E5438>g<#73593B>g
    // {bold:1b, extra: [{text:"test",color:"#392216"}]}
    // {"bold":true, extra: [{"color":"#392216","text":"C"},{"color":"#3E2719","text":"h"},{"color":"#432B1C","text":"o"},{"color":"#48301F","text":"c"},{"color":"#4C3422","text":"o"},{"color":"#513925","text":"l"},{"color":"#563E29","text":"a"},{"color":"#5B422C","text":"t"},{"color":"#60472F","text":"e "},{"color":"#695035","text":"E"},{"color":"#6E5438","text":"g"},{"color":"#73593B","text":"g"}],"text":""}
    // {"bold":true,"color":"#392216","extra":[{"color":"#3E2719","extra":[{"color":"#432B1C","extra":[{"color":"#48301F","extra":[{"color":"#4C3422","extra":[{"color":"#513925","extra":[{"color":"#563E29","extra":[{"color":"#5B422C","extra":[{"color":"#60472F","extra":[{"color":"#695035","extra":[{"color":"#6E5438","extra":[{"color":"#73593B","text":"g"}],"text":"g"}],"text":"E"}],"text":"e "}],"text":"t"}],"text":"a"}],"text":"l"}],"text":"o"}],"text":"c"}],"text":"o"}],"text":"h"}],"text":"C"}
}
