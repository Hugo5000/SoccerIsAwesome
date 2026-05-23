package at.iamsoccer.soccerisawesome.chatgroups;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

public class ChatRenderer implements io.papermc.paper.chat.ChatRenderer {
    @Override
    public Component render(Player source, Component sourceDisplayName, Component message, Audience viewer) {
        return Component.empty()
            .append(Component.text("Group", NamedTextColor.DARK_AQUA))
            .append(Component.text(" | ", NamedTextColor.AQUA))
            .append(sourceDisplayName)
            .append(Component.text(": "))
            .append(message);
    }
}
