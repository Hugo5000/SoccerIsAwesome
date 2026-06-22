package at.iamsoccer.soccerisawesome.biomes;

import at.iamsoccer.soccerisawesome.AbstractModule;
import at.iamsoccer.soccerisawesome.SoccerIsAwesomePlugin;
import co.aikar.commands.CommandManager;
import co.aikar.commands.PaperCommandManager;
import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import me.outspending.biomesapi.biome.CustomBiome;
import me.outspending.biomesapi.keys.ResourceKey;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;

import java.util.Collections;

public class BiomesModule extends AbstractModule {
    private final BiomeDialogFactory biomeDialogFactory = new BiomeDialogFactory();
    public BiomesModule(SoccerIsAwesomePlugin plugin) {
        super(plugin, "Biomes");
    }

    @Override
    public boolean enable(PaperCommandManager commandManager) {
        if(!super.enable(commandManager)) return false;

        CustomBiome.builder(ResourceKey.of("shia", "custom"))
            .build()
            .register();

        return true;
    }

    @Override
    public void lifecycleHandler(SoccerIsAwesomePlugin.ICommandRegistration register) {
        register.register(Commands.literal("biomes")
                .requires(css -> css.getSender() instanceof Player)
            .executes(ctx -> {
                var player = (Player)ctx.getSource().getSender();
                biomeDialogFactory.open(player);
                return Command.SINGLE_SUCCESS;
        }).build(), "Allows modification of biomes", Collections.emptyList());
    }
}
