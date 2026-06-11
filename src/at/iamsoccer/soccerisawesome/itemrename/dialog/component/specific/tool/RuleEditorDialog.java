package at.iamsoccer.soccerisawesome.itemrename.dialog.component.specific.tool;

import at.iamsoccer.soccerisawesome.itemrename.dialog.component.AbstractDataComponentEditorDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.AbstractDialogFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.DialogButton;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Tool;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.tag.Tag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.util.TriState;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static net.kyori.adventure.text.minimessage.tag.Tag.preProcessParsed;

@SuppressWarnings("UnstableApiUsage")
public class RuleEditorDialog extends AbstractDataComponentEditorDialog<Tool> {
    private final int index;

    public RuleEditorDialog(int index, Supplier<AbstractDialogFactory<Player>> returnDialogFactorySupplier) {
        super(returnDialogFactorySupplier, DataComponentTypes.TOOL);
        this.index = index;
    }

    @Override
    public List<DialogInput> parseResponseToInputs(@Nullable DialogResponseView response, ItemStack itemStack, @Nullable Tool currentComponent) {
        if (currentComponent == null) return List.of();
        @Nullable var rule = currentComponent.rules().size() - 1 < index ? null : currentComponent.rules().get(index);
        return List.of(
            DialogInput.numberRange("mining_speed", Component.text("Mining Speed"), -0.1f, 1_000)
                .initial(getFloat(response, "mining_speed", () -> rule != null && rule.speed() != null ? rule.speed() : -0.1f))
                .step(0.1f).width(200).build(),
            DialogInput.singleOption("correct_tool", Component.text("Correct Tool"), List.of(
                SingleOptionDialogInput.OptionEntry.create("eh", Component.text("Eh?"), rule == null || rule.correctForDrops() == TriState.NOT_SET),
                SingleOptionDialogInput.OptionEntry.create("true", Component.text("Yes"), rule != null && rule.correctForDrops() == TriState.TRUE),
                SingleOptionDialogInput.OptionEntry.create("no", Component.text("No"), rule != null && rule.correctForDrops() == TriState.FALSE)
            )).build(),
            DialogInput.text("blocks", Component.text("Blocks"))
                .initial(getString(response, "blocks", () -> rule != null ? parseTag(rule.blocks()) : ""))
                .multiline(TextDialogInput.MultilineOptions.create(null, 50))
                .maxLength(1024)
                .build()
        );
    }

    @Override
    public @Nullable Tool parseResponseToComponent(DialogResponseView response, ItemStack itemStack, @Nullable Tool currentComponent) {
        if (currentComponent == null) return null;
        var rules = new ArrayList<>(currentComponent.rules());
        Float speed = getFloat(response, "mining_speed", () -> 1f);
        var correctTool = switch (getString(response, "correct_tool", () -> "eh")) {
            case "true" -> TriState.TRUE;
            case "no" -> TriState.FALSE;
            default -> TriState.NOT_SET;
        };
        var newRule = Tool.rule(
            getTag(getString(response, "blocks", () -> ""), RegistryKey.BLOCK),
            speed < 0 ? null : speed,
            correctTool
        );
        if (index >= rules.size()) rules.add(newRule);
        rules.set(index, newRule);
        var equippable = Tool.tool()
            .defaultMiningSpeed(currentComponent.defaultMiningSpeed())
            .canDestroyBlocksInCreative(currentComponent.canDestroyBlocksInCreative())
            .damagePerBlock(currentComponent.damagePerBlock())
            .addRules(rules);
        return equippable.build();
    }

    @Override
    protected boolean tryOpen(Player player) {
        if (!super.tryOpen(player)) return false;
        Tool tool = player.getInventory().getItemInMainHand().getData(DataComponentTypes.TOOL);
        if (tool == null) {
            returnToPrevious(player);
            return false;
        }
        return true;
    }

    @Override
    public TagResolver tagResolver(Player player, @Nullable DialogResponseView response) {
        var rules = player.getInventory().getItemInMainHand().getData(dataComponentType).rules();
        if (rules.size() >= index) return super.tagResolver(player, response);
        var rule = rules.get(index);
        return TagResolver.builder()
            .resolver(super.tagResolver(player, response))
            .tag("blocks", preProcessParsed(rule.blocks() instanceof Tag<BlockType> tag ? tag.tagKey().key().asMinimalString() : rule.blocks().size() + " Blocks"))
            .tag("right_tool", preProcessParsed(rule.correctForDrops().toBooleanOrElse(false) ? "Correct Tool" : "Wrong Tool"))
            .tag("speed", preProcessParsed(String.valueOf(rule.speed())))
            .build();
    }

    @Override
    protected DialogButton.IButtonInfoSupplier<Player> buttonInfoSupplier(String name) {
        if (name.equals("external")) {
            return player -> {
                var tool = player.getInventory().getItemInMainHand().getData(DataComponentTypes.TOOL);
                var rule = tool.rules().get(index);
                return new DialogButton.ButtonInfo(
                    Component.text("Edit Rule #" + (index + 1)),
                    Component.text()
                        .append(Component.text("Speed: " + rule.speed()))
                        .appendNewline()
                        .append(Component.text("Correct tool: " + rule.correctForDrops()))
                        .appendNewline()
                        .append(Component.text("Blocks: "))
                        .append(Component.text(parseTag(rule.blocks())))
                        .build()
                );
            };
        } else if (name.equals("remove-component")) {
            return player -> new DialogButton.ButtonInfo(
                Component.text("Remove Rule"),
                null
            );
        }
        return super.buttonInfoSupplier(name);
    }

    @Override
    protected boolean resetAble() {
        return false;
    }

    @Override
    protected void removeComponent(ItemStack item) {
        var tool = item.getData(DataComponentTypes.TOOL);
        if (tool == null) return;
        var rules = new ArrayList<>(tool.rules());
        rules.remove(index);
        item.setData(DataComponentTypes.TOOL, Tool.tool()
            .defaultMiningSpeed(tool.defaultMiningSpeed())
            .canDestroyBlocksInCreative(tool.canDestroyBlocksInCreative())
            .damagePerBlock(tool.damagePerBlock())
            .addRules(rules));
    }
}
