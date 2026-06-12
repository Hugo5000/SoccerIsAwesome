package at.iamsoccer.soccerisawesome.itemrename.dialog.component.specific.tool;

import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractButtonListDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.AbstractDialogFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.DialogButton;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Tool;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import static net.kyori.adventure.text.minimessage.tag.Tag.preProcessParsed;

@SuppressWarnings("UnstableApiUsage")
public class RulesListDialog extends AbstractButtonListDialog {
    private final HashMap<Integer, RuleEditorDialog> selectRuleButtons = new HashMap<>();

    private final DialogButton<Player> addRuleButton = newButton("add-tool-rule", (response, player) -> {
        if (!tryOpen(player)) return;
        var tool = player.getInventory().getItemInMainHand().getData(DataComponentTypes.TOOL);
        getRuleEditor(tool.rules().size()).open(player);
    });

    private RuleEditorDialog getRuleEditor(int index) {
        return selectRuleButtons.compute(index, (integer, ruleEditorDialog) ->
            ruleEditorDialog != null ? ruleEditorDialog : new RuleEditorDialog(integer, () -> this)
        );
    }

    public RulesListDialog(@Nullable Permission permission, Supplier<AbstractDialogFactory<Player>> returnDialogFactorySupplier) {
        super(permission, returnDialogFactorySupplier);
        for (int i = 0; i < 20; i++) {
            getRuleEditor(i);
        }
    }

    @Override
    public void reload(YamlFileConfig configFile, ConfigurationSection configSection) {
        super.reload(configFile, configSection);
        selectRuleButtons.values().forEach(ruleEditorDialog -> ruleEditorDialog.reload(configFile, configSection));
    }

    @Override
    protected List<ActionButton> getDialogButtons(Player player) {
        var tool = player.getInventory().getItemInMainHand().getData(DataComponentTypes.TOOL);
        var list = new ArrayList<ActionButton>(tool.rules().size() + 1);
        for (int i = 0; i < tool.rules().size(); i++) {
            list.add(getRuleEditor(i).externalButton().button(player, 200));
        }
        list.add(addRuleButton.button(player, 200));
        return list;
    }

    @Override
    protected boolean tryOpen(Player player) {
        if (!super.tryOpen(player)) return false;
        var tool = player.getInventory().getItemInMainHand().getData(DataComponentTypes.TOOL);
        if (tool == null) {
            returnToPrevious(player);
            return false;
        }
        return true;
    }

    private DialogButton<Player> createNewButton(int slot) {
        return new DialogButton<>(userClass, player -> {
            var tool = player.getInventory().getItemInMainHand().getData(DataComponentTypes.TOOL);
            return new DialogButton.ButtonInfo(
                Component.text("Add new Rule"), null
            );
        }, (response, player) -> {
            open(player, response);
        });
    }

    @Override
    protected DialogButton.IButtonInfoSupplier<Player> buttonInfoSupplier(String name) {
        if (name.equals("external")) {
            return player -> {
                var tool = player.getInventory().getItemInMainHand().getData(DataComponentTypes.TOOL);
                return new DialogButton.ButtonInfo(
                    Component.text("Edit Rules"),
                    getRules(tool)
                );
            };
        }
        return super.buttonInfoSupplier(name);
    }

    @Override
    public TagResolver tagResolver(Player player, @Nullable DialogResponseView response) {
        return TagResolver.builder()
            .resolver(super.tagResolver(player, response))
            .tag("component", preProcessParsed(DataComponentTypes.TOOL.key().asMinimalString()))
            .build();
    }

    private Component getRules(@Nullable Tool tool) {
        if (tool == null || tool.rules().isEmpty()) return Component.text("No active Rules", NamedTextColor.YELLOW);
        var res = Component.text();
        res.content("Currently Active Rules: " + tool.rules().size());
        return res.build();
    }
}
