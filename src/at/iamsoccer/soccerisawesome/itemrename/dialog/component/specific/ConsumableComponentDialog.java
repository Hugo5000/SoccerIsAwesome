package at.iamsoccer.soccerisawesome.itemrename.dialog.component.specific;

import at.iamsoccer.soccerisawesome.itemrename.dialog.component.AbstractDataComponentEditorDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractDialogFactory;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class ConsumableComponentDialog extends AbstractDataComponentEditorDialog<Consumable> {
    public ConsumableComponentDialog(Supplier<AbstractDialogFactory<Player>> returnDialogFactorySupplier) {
        super(returnDialogFactorySupplier, DataComponentTypes.CONSUMABLE);
    }


    @Override
    public List<DialogInput> parseResponseToInputs(@Nullable DialogResponseView response, ItemStack itemStack, @Nullable Consumable currentComponent) {
        return List.of(
            DialogInput.numberRange("consume_seconds", Component.text("Consumtion time"), 0, 5)
                .initial(getValue(response, "consume_seconds", currentComponent != null ? currentComponent.consumeSeconds() : 1.6f))
                .step(0.1f).width(200).build(),
            DialogInput.singleOption("slot", Component.text("Equipment Slot"), List.of(
                createOption(ItemUseAnimation.NONE, "None", response, currentComponent, false, Consumable::animation),
                createOption(ItemUseAnimation.EAT, "Eat", response, currentComponent, true, Consumable::animation),
                createOption(ItemUseAnimation.DRINK, "Drink", response, currentComponent, false, Consumable::animation),
                createOption(ItemUseAnimation.BLOCK, "Block", response, currentComponent, false, Consumable::animation),
                createOption(ItemUseAnimation.BOW, "Bow", response, currentComponent, false, Consumable::animation),
                createOption(ItemUseAnimation.SPEAR, "Spear",response, currentComponent, false, Consumable::animation),
                createOption(ItemUseAnimation.CROSSBOW, "Crossbow",response, currentComponent, false, Consumable::animation),
                createOption(ItemUseAnimation.SPYGLASS, "Spyglass",response, currentComponent, false, Consumable::animation),
                createOption(ItemUseAnimation.TOOT_HORN, "Toot Horn",response, currentComponent, false, Consumable::animation),
                createOption(ItemUseAnimation.BRUSH, "Brush",response, currentComponent, false, Consumable::animation),
                createOption(ItemUseAnimation.BUNDLE, "Bundle",response, currentComponent, false, Consumable::animation)
            )).build(),
            createKeyInput("sound", "Sound", response, currentComponent, Consumable::sound),
            DialogInput.bool("particles", Component.text("Consume Particles"))
                .initial(currentComponent != null ? currentComponent.hasConsumeParticles() : true)
                .build()
        );
    }

    @Override
    public @Nullable Consumable parseResponseToComponent(DialogResponseView response, ItemStack itemStack, @Nullable Consumable currentComponent) {
        ItemUseAnimation useAnimation;
        try {
            useAnimation = ItemUseAnimation.valueOf(getValue(response, "animation", ItemUseAnimation.EAT.name()));
        } catch (IllegalArgumentException e) {
            return null;
        }
        var sound = getValue(response, "sound", "");
        var consumable = Consumable.consumable()
            .consumeSeconds(getValue(response, "consume_seconds", 1.6f))
            .animation(useAnimation)
            .hasConsumeParticles(getValue(response, "particles", true));
        if (!sound.isBlank() && Key.parseable(sound)) {
            consumable.sound(Key.key(sound));
        }
        return consumable.build();
    }
}
