package at.iamsoccer.soccerisawesome.operator;

import at.iamsoccer.soccerisawesome.AbstractModule;
import at.iamsoccer.soccerisawesome.SoccerIsAwesomePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class FakeOperatorModule extends AbstractModule implements Listener {
    private static final byte OP_LEVEL_FOR_STUFFS = 2;

    public FakeOperatorModule(SoccerIsAwesomePlugin plugin) {
        super(plugin, "FakeOperator");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("shia.fakeop")) {
            player.sendOpLevel(OP_LEVEL_FOR_STUFFS);
        }
    }
}
