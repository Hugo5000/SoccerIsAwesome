package at.iamsoccer.soccerisawesome.shouldersit;

import at.iamsoccer.soccerisawesome.AbstractModule;
import at.iamsoccer.soccerisawesome.SoccerIsAwesomePlugin;
import co.aikar.commands.PaperCommandManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemType;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.UUID;

public class ShoulderSitModule extends AbstractModule implements Listener {
    private HashMap<UUID, ShoulderTask> tasks = new HashMap<>();
    private Team team;

    public ShoulderSitModule(SoccerIsAwesomePlugin plugin) {
        super(plugin, "ShoulderSit");
    }

    @Override
    public boolean enable(PaperCommandManager commandManager) {
        if (!super.enable(commandManager)) return false;
        Bukkit.getScheduler().runTask(plugin, () -> {
            team = plugin.getServer().getScoreboardManager().getMainScoreboard().getTeam("entities_with_no_collision");
            if (team == null) {
                team = plugin.getServer().getScoreboardManager().getMainScoreboard().registerNewTeam("entities_with_no_collision");
                team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            }
        });
        return true;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        var shoulderEntity = event.getPlayer().getWorld().spawn(event.getPlayer().getEyeLocation(), ItemDisplay.class, entity -> {
            entity.setItemStack(ItemType.GRASS_BLOCK.createItemStack());
            entity.setPersistent(false);
            entity.setTransformation(new Transformation(
                new Vector3f(0, 0, 0),
                new Quaternionf(),
                new Vector3f(0.1f, 0.1f, 0.1f),
                new Quaternionf())
            );
        });
        ShoulderTask task = new ShoulderTask(event.getPlayer(), shoulderEntity);
        tasks.put(event.getPlayer().getUniqueId(), task);
        shoulderEntity.getWorld().spawn(shoulderEntity.getLocation(), Chicken.class, chicken -> {
            chicken.setPersistent(false);
            shoulderEntity.addPassenger(chicken);
            team.addEntity(chicken);
            chicken.setCollidable(false);
        });
        task.runTaskTimer(plugin, 0,0);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLeave(PlayerQuitEvent event) {
        tasks.remove(event.getPlayer().getUniqueId()).cancel();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {

    }


}
