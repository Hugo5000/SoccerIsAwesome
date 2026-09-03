package at.iamsoccer.soccerisawesome.biomes;

import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.ConfigDialogFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.DialogButton;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import me.outspending.biomesapi.keys.KeyChains;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class BiomeDialogFactory extends ConfigDialogFactory<Player> {
    public BiomeDialogFactory() {
        super(Player.class, null);
    }

    @Override
    protected DialogType dialogType(DialogButton<Player> closeButton, Player player, @Nullable DialogResponseView response) {
        var buttons = KeyChains.BIOMES.items().stream()
            .map(biome -> biome.getKey().asMinimalString())
            .map(key -> ActionButton.builder(Component.text(key)).build())
            .toList();
        return DialogType.multiAction(buttons)
            .columns(1)
            .exitAction(closeButton.button(player))
            .build();
    }
}
