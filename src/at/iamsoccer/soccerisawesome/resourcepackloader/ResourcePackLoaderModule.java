package at.iamsoccer.soccerisawesome.resourcepackloader;

import at.hugob.plugin.library.config.MiniMsgLegacyHybridSerializer;
import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.AbstractModule;
import at.iamsoccer.soccerisawesome.SoccerIsAwesomePlugin;
import co.aikar.commands.PaperCommandManager;
import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.resource.ResourcePackCallback;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.resource.ResourcePackStatus;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class ResourcePackLoaderModule extends AbstractModule implements Listener {
    private YamlFileConfig config;

    private ResourcePackRequest global = null;
    private final Map<String, ResourcePackRequest> worldPacks = new HashMap<>();

    public ResourcePackLoaderModule(SoccerIsAwesomePlugin plugin) {
        super(plugin, "ResourcePackLoader");
    }

    @EventHandler(ignoreCancelled = false)
    public void onPlayerJoin(AsyncPlayerConnectionConfigureEvent event) {
        if (global == null) return;
        try {
            var res = sendResourcePack(event.getConnection().getAudience(), global).get();
            if (res) return;
        } catch (Exception ignored) {
        }
        event.getConnection().disconnect(Component.text("Resource Pack loading failed"));
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
    public void onPlayerJoinWorld(PlayerJoinEvent event) {
        var toPack = worldPacks.get(event.getPlayer().getWorld().key().asString());
        if (toPack == null) return;
        sendResourcePack(event.getPlayer(), toPack).thenAcceptAsync(res -> {
            if (!res) event.getPlayer().kick(Component.text("Resource Pack loading failed"));
        }, Bukkit.getScheduler().getMainThreadExecutor(plugin));
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        var fromPack = worldPacks.get(event.getFrom().key().asString());
        if (fromPack != null)
            event.getPlayer().removeResourcePacks(fromPack.packs().stream().map(ResourcePackInfo::id).toList());
        var toPack = worldPacks.get(event.getPlayer().getWorld().key().asString());
        if (toPack == null) return;
        sendResourcePack(event.getPlayer(), toPack).thenAcceptAsync(res -> {
            if (!res) event.getPlayer().kick(Component.text("Resource Pack loading failed"));
        }, Bukkit.getScheduler().getMainThreadExecutor(plugin));
    }

    private static CompletableFuture<Boolean> sendResourcePack(Audience audience, ResourcePackRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            var latch = new CountDownLatch(request.packs().size());
            var failed = new AtomicBoolean(false);

            var packs = request.packs().stream().map(ResourcePackInfo::id).collect(Collectors.toCollection(HashSet::new));
            audience.sendResourcePacks(
                ResourcePackRequest.resourcePackRequest(request)
                    .callback((uuid, status, aud) -> {
                        if (!packs.contains(uuid)) return;
                        switch (status) {
                            case FAILED_DOWNLOAD:
                            case DECLINED:
                            case FAILED_RELOAD:
                            case DISCARDED:
                            case INVALID_URL:
                                failed.set(true);
                            case SUCCESSFULLY_LOADED:
                                latch.countDown();
                                break;
                        }
                    })
            );
            try {
                latch.await(10, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
            }
            return latch.getCount() == 0 && !(failed.get() && request.required());
        });
    }

    @Override
    public boolean enable(PaperCommandManager commandManager) {
        if (!super.enable(commandManager)) return false; // should never fail
        config = new YamlFileConfig(plugin, "resourcepack-config.yml");
        return true;
    }

    @Override
    public void reload() {
        config.reload();
        var newPacks = parsePacks(config.getMapList("global.packs"));
        if (newPacks.isEmpty()) global = null;
        else
            global = ResourcePackRequest.resourcePackRequest().packs(newPacks).required(true).prompt(MiniMsgLegacyHybridSerializer.INSTANCE.deserialize(config.getString("global.prompt"))).build();
        var worlds = config.getConfigurationSection("worlds");
        if (worlds != null) {
            worldPacks.clear();
            for (var worldName : worlds.getKeys(false)) {
                var key = Key.key(worldName).asString();
                var world = worlds.getConfigurationSection(worldName);
                var packs = parsePacks(world.getMapList("packs"));
                if (packs.isEmpty()) continue;
                worldPacks.put(key, ResourcePackRequest.resourcePackRequest().packs(packs).required(true).prompt(MiniMsgLegacyHybridSerializer.INSTANCE.deserialize(world.getString("prompt"))).build());
            }
        }
        Bukkit.getOnlinePlayers().forEach(player -> player.clearResourcePacks());
        if (global != null) Bukkit.getOnlinePlayers().forEach(player -> sendResourcePack(player, global));
        for (Player player : Bukkit.getOnlinePlayers()) {
            var pack = worldPacks.get(player.getWorld().key().asString());
            if (pack == null) continue;
            sendResourcePack(player, pack);
        }
    }

    private static List<ResourcePackInfo> parsePacks(List<Map<?, ?>> maps) {
        if (maps == null || maps.isEmpty()) return Collections.emptyList();
        var newPacks = new ArrayList<ResourcePackInfo>();
        for (Map<?, ?> map : maps) {
            if (!(map.get("name") instanceof String name)) continue;
            if (!(map.get("url") instanceof String url)) continue;
            if (!(map.get("hash") instanceof String hash)) continue;
            newPacks.add(ResourcePackInfo.resourcePackInfo(UUID.nameUUIDFromBytes(name.getBytes()), URI.create(url), hash));
        }
        return newPacks;
    }
}
