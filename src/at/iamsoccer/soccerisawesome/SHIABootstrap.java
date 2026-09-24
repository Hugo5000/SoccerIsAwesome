package at.iamsoccer.soccerisawesome;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.logging.Level;

public class SHIABootstrap implements PluginBootstrap {
    @Override
    public void bootstrap(BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.DATAPACK_DISCOVERY.newHandler(event -> {
            try {
                var lootTableDatapackURI = this.getClass().getResource("/datapack/loot_tables").toURI();
                event.registrar().discoverPack(lootTableDatapackURI, "LootTableAdditions");
            } catch (URISyntaxException | IOException e) {
                throw new RuntimeException(e);
            }
            if(Files.isDirectory(context.getDataDirectory().resolve("datapack"))) {
                var customDatapack = context.getDataDirectory().resolve("datapack").toUri();
                try {
                    event.registrar().discoverPack(customDatapack, "CustomSHIADatapack");
                } catch (IOException e) {
                    Bukkit.getLogger().log(Level.SEVERE, "Failed to load custom datapack", e);
                }
            }
        }));
    }
}
