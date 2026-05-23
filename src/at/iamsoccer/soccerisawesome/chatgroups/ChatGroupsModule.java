package at.iamsoccer.soccerisawesome.chatgroups;

import at.hugob.plugin.library.config.MiniMsgLegacyHybridSerializer;
import at.iamsoccer.soccerisawesome.AbstractModule;
import at.iamsoccer.soccerisawesome.SoccerIsAwesomePlugin;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.mineacademy.chatcontrol.api.ChatControlAPI;

public class ChatGroupsModule extends AbstractModule implements Listener {
    private final static boolean SINGED_CHAT_MESSAGES = false;
    private final ChatRenderer renderer = new ChatRenderer();

    public ChatGroupsModule(SoccerIsAwesomePlugin plugin) {
        super(plugin, "ChatGroups");
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.NORMAL)
    public void onChatNormal(AsyncChatEvent event) {
        var cache = ChatControlAPI.getCache(event.getPlayer());
        if (cache.hasConversingPlayer()) {
            Bukkit.broadcast(Component.text(event.getPlayer().getName() + " is currently DMing", NamedTextColor.RED));
            return;
        }
        var channel = cache.getWriteChannel();
        if (channel != null) {
            Bukkit.broadcast(Component.text(event.getPlayer().getName() + " is writing in " + channel.getName(), NamedTextColor.RED));
            return;
        }
        Bukkit.broadcast(Component.text("You're writing in group ^^", NamedTextColor.GREEN));
        event.renderer(renderer);
        event.message(MiniMsgLegacyHybridSerializer.INSTANCE.deserialize(event.signedMessage().message()));
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
    public void onChatHigh(AsyncChatEvent event) {
        // here does chat control do its thing
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGHEST)
    public void onChatHighest(AsyncChatEvent event) {
        if (event.isCancelled() && event.renderer() instanceof ChatRenderer) {
            event.setCancelled(false);
        }
        if(!SINGED_CHAT_MESSAGES) {
            var players = event.viewers().stream().filter(Player.class::isInstance).toList();
            players.forEach(event.viewers()::remove);
            for (Audience player : players) {
                player.sendMessage(event.renderer().render(event.getPlayer(), event.getPlayer().displayName(), event.message(), player));
            }
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
    public void onChatMonitor(AsyncChatEvent event) {
//        sendMessage(event, EventPriority.MONITOR);
    }

    private static void sendMessage(AsyncChatEvent event, EventPriority priority) {
        Bukkit.broadcast(Component.text("viewers: " + event.viewers()));
        Bukkit.broadcast(Component.text("renderer: " + event.renderer().getClass().getSimpleName()));
        Bukkit.getOnlinePlayers().forEach(viewer -> viewer.sendMessage(Component.text(priority + ": " + event.isCancelled() + " Message: ")
            .append(event.renderer().render(event.getPlayer(), event.getPlayer().displayName(), event.message(), viewer)))
        );
    }
}
