package at.iamsoccer.soccerisawesome;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import java.io.IOException;
import java.net.URISyntaxException;

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
        }));
    }
}
