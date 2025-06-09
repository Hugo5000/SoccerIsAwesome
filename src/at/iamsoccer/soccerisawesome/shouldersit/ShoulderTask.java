package at.iamsoccer.soccerisawesome.shouldersit;

import io.papermc.paper.entity.TeleportFlag;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.joml.Quaterniond;
import org.joml.Vector3d;

public class ShoulderTask extends BukkitRunnable {
    public final Player player;
    public final Entity entity;

    public ShoulderTask(Player player, Entity entity) {
        this.player = player;
        this.entity = entity;
    }

    @Override
    public void run() {
        float bodyYaw = player.getBodyYaw();
        double playerScale = player.getAttribute(Attribute.SCALE).getValue();
        var offset = new Quaterniond().rotateY(-org.joml.Math.toRadians(bodyYaw)).transform(new Vector3d(.5, 0, 0).mul(playerScale));
        entity.teleport(player.getEyeLocation()
                .add(offset.x, offset.y, offset.z)
                .setRotation(bodyYaw, 0),
            TeleportFlag.EntityState.RETAIN_PASSENGERS
        );
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        super.cancel();
        entity.remove();
    }
}
