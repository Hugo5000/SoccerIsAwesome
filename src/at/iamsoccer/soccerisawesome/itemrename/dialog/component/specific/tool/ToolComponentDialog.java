package at.iamsoccer.soccerisawesome.itemrename.dialog.component.specific.tool;

import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.itemrename.dialog.component.AbstractDataComponentEditorDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.AbstractDialogFactory;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Tool;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class ToolComponentDialog extends AbstractDataComponentEditorDialog<Tool> {
    private final RulesListDialog rulesListDialog = new RulesListDialog(permission, () -> this);

    public ToolComponentDialog(Supplier<AbstractDialogFactory<Player>> returnDialogFactorySupplier) {
        super(returnDialogFactorySupplier, DataComponentTypes.TOOL);
    }

    @Override
    public void reload(YamlFileConfig configFile, ConfigurationSection configSection) {
        super.reload(configFile, configSection);
        rulesListDialog.reload(configFile, configSection);
    }

    @Override
    public List<DialogInput> parseResponseToInputs(@Nullable DialogResponseView response, ItemStack itemStack, @Nullable Tool currentComponent) {
        return List.of(
            DialogInput.numberRange("damage_per_block", Component.text("Damage per Block"), 0, 20)
                .initial(getFloat(response, "damage_per_block", () -> currentComponent != null ? currentComponent.damagePerBlock() : 0f))
                .step(1f).width(200).build(),
            DialogInput.numberRange("mining_speed", Component.text("Default Mining Speed"), 0, 1_000)
                .initial(getFloat(response, "mining_speed", () -> currentComponent != null ? currentComponent.defaultMiningSpeed() : 0f))
                .step(0.1f).width(200).build(),
            DialogInput.bool("can_destroy_blocks_in_creative", Component.text("Can Destroy Blocks in Creative"))
                .initial(getBoolean(response, "can_destroy_blocks_in_creative", () -> currentComponent != null ? currentComponent.canDestroyBlocksInCreative() : true))
                .build()
        );
    }

    @Override
    public @Nullable Tool parseResponseToComponent(DialogResponseView response, ItemStack itemStack, @Nullable Tool currentComponent) {
        var equippable = Tool.tool()
            .defaultMiningSpeed(getFloat(response, "mining_speed", () -> 1f))
            .canDestroyBlocksInCreative(getBoolean(response, "can_destroy_blocks_in_creative", () -> true))
            .damagePerBlock(getFloat(response, "damage_per_block", () -> 0f).intValue())
            .addRules(currentComponent.rules());
        return equippable.build();
    }

    @Override
    protected List<ActionButton> getDialogButtons(Player player) {
        var buttons = super.getDialogButtons(player);
        buttons.add(0, rulesListDialog.externalButton().button(player));
        return buttons;
    }
}
