package at.iamsoccer.soccerisawesome.itemrename.dialog.component.specific;

import at.iamsoccer.soccerisawesome.itemrename.dialog.component.AbstractDataComponentEditorDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractDialogFactory;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Equippable;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class EquipableComponentDialog extends AbstractDataComponentEditorDialog<Equippable> {
    public EquipableComponentDialog(Supplier<AbstractDialogFactory<Player>> returnDialogFactorySupplier) {
        super(returnDialogFactorySupplier, DataComponentTypes.EQUIPPABLE);
    }

    @Override
    public List<DialogInput> parseResponseToInputs(@Nullable DialogResponseView response, ItemStack itemStack, @Nullable Equippable currentComponent) {
        return List.of(
            DialogInput.singleOption("slot", Component.text("Equipment Slot"), List.of(
                createOption(EquipmentSlot.HEAD, "Head", response, currentComponent, true, Equippable::slot),
                createOption(EquipmentSlot.CHEST, "Chest", response, currentComponent, false, Equippable::slot),
                createOption(EquipmentSlot.LEGS, "Legs", response, currentComponent, false, Equippable::slot),
                createOption(EquipmentSlot.FEET, "Feet", response, currentComponent, false, Equippable::slot),
                createOption(EquipmentSlot.BODY, "Body", response, currentComponent, false, Equippable::slot),
                createOption(EquipmentSlot.SADDLE, "Saddle", response, currentComponent, false, Equippable::slot)
            )).build(),
            createKeyInput("asset_id", "Asset ID", response, currentComponent, Equippable::assetId),
            createKeyInput("camera_overlay", "Camera Overlay", response, currentComponent, Equippable::cameraOverlay),
            createKeyInput("equip_sound", "Equip Sound", response, currentComponent, Equippable::equipSound),
            createKeyInput("shear_sound", "Shear Sound", response, currentComponent, Equippable::shearSound),
            DialogInput.bool("can_be_sheared", Component.text("Can be sheared"))
                .initial(getValue(response, "can_be_sheared", currentComponent != null ? currentComponent.canBeSheared() : false))
                .build(),
            DialogInput.bool("damage_on_hurt", Component.text("Damage on Hurt"))
                .initial(getValue(response, "damage_on_hurt", currentComponent != null ? currentComponent.damageOnHurt() : false))
                .build(),
            DialogInput.bool("dispensable", Component.text("Dispensable"))
                .initial(getValue(response, "dispensable", currentComponent != null ? currentComponent.dispensable() : false))
                .build(),
            DialogInput.bool("equip_on_interact", Component.text("Equip on Interact"))
                .initial(getValue(response, "equip_on_interact", currentComponent != null ? currentComponent.equipOnInteract() : false))
                .build(),
            DialogInput.bool("swappable", Component.text("Swappable"))
                .initial(getValue(response, "swappable", currentComponent != null ? currentComponent.swappable() : false))
                .build(),
            DialogInput.text("allowed_entities", Component.text("Allowed Entities"))
                .initial(getValue(response, "allowed_entities", currentComponent != null ? parseEntities(currentComponent.allowedEntities()) : ""))
                .multiline(TextDialogInput.MultilineOptions.create(null, 50))
                .maxLength(1024)
                .build()
        );
    }

    @Override
    public @Nullable Equippable parseResponseToComponent(DialogResponseView response, ItemStack itemStack, @Nullable Equippable currentComponent) {
        EquipmentSlot slot;
        try {
            slot = EquipmentSlot.valueOf(getValue(response, "slot", "HEAD"));
        } catch (IllegalArgumentException e) {
            return null;
        }
        Bukkit.getLogger().info(slot.name());
        var asset_id = getValue(response, "asset_id", "");
        var camera_overlay = getValue(response, "camera_overlay", "");
        var equip_sound = getValue(response, "equip_sound", "");
        var shear_sound = getValue(response, "shear_sound", "");
        var equippable = Equippable.equippable(slot)
            .assetId(asset_id.isBlank() || !Key.parseable(asset_id) ? null : Key.key(asset_id))
            .cameraOverlay(camera_overlay.isBlank() || !Key.parseable(camera_overlay) ? null : Key.key(camera_overlay))
            .canBeSheared(getValue(response, "can_be_sheared", false))
            .damageOnHurt(getValue(response, "damage_on_hurt", false))
            .dispensable(getValue(response, "dispensable", false))
            .equipOnInteract(getValue(response, "equip_on_interact", false))
            .swappable(getValue(response, "swappable", false))
            .allowedEntities(parseEntities(getValue(response, "allowed_entities", "")));
        if (!equip_sound.isBlank() && Key.parseable(equip_sound)) {
            equippable.equipSound(Key.key(equip_sound));
        }
        if (!shear_sound.isBlank() && Key.parseable(shear_sound)) {
            equippable.shearSound(Key.key(shear_sound));
        }
        return equippable.build();
    }
}
