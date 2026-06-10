package at.iamsoccer.soccerisawesome.itemrename;

import at.hugob.plugin.library.config.ConfigUtils;
import at.hugob.plugin.library.config.MiniMsgLegacyHybridSerializer;
import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.AbstractModule;
import at.iamsoccer.soccerisawesome.SoccerIsAwesomePlugin;
import at.iamsoccer.soccerisawesome.itemrename.dialog.MainRenameDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.rename.ItemCustomNameRenameDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.rename.ItemLoreRenameDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.rename.ItemNameRenameDialog;
import co.aikar.commands.PaperCommandManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.datacomponent.DataComponentType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ItemRenameModule extends AbstractModule implements Listener {
    public final ItemCustomNameRenameDialog itemCustomNameRenameDialog = new ItemCustomNameRenameDialog(plugin.getServer().getPluginManager().getPermission("shia.rename.custom-name"), null);
    public final ItemNameRenameDialog itemNameRenameDialog = new ItemNameRenameDialog(plugin.getServer().getPluginManager().getPermission("shia.rename.item-name"), null);
    public final ItemLoreRenameDialog itemLoreRenameDialog = new ItemLoreRenameDialog(plugin.getServer().getPluginManager().getPermission("shia.rename.lore"), null);

    public final MainRenameDialog mainRenameDialog = new MainRenameDialog(plugin.getServer().getPluginManager().getPermission("shia.rename.command"));

    private Component noItemInMainHand = Component.empty();

    @SuppressWarnings("NotNullFieldNotInitialized")
    private YamlFileConfig config;

    public ItemRenameModule(SoccerIsAwesomePlugin plugin) {
        super(plugin, "ItemRename", new AnvilListener());
    }

    @Override
    public boolean enable(PaperCommandManager commandManager) {
        if (!super.enable(commandManager)) return false;
        config = new YamlFileConfig(plugin, "rename-config.yml");
        return true;
    }

    @Override
    public void reload() {
        config.reload();
        itemCustomNameRenameDialog.reload(config, config.getConfigurationSection("dialog.custom-name"));
        itemNameRenameDialog.reload(config, config.getConfigurationSection("dialog.item-name"));
        itemLoreRenameDialog.reload(config, config.getConfigurationSection("dialog.lore"));
        mainRenameDialog.reload(config, config.getConfigurationSection("dialog.main"));

        noItemInMainHand = ConfigUtils.parseComponent(config, config.getString("no-item-in-main-hand"), null, null);
        FORMAT_INFOS.clear();
        for (String format : config.getConfigurationSection("format-infos").getKeys(false)) {
            if (!config.isString("format-infos." + format)) continue;
            FORMAT_INFOS.put(format, MiniMsgLegacyHybridSerializer.INSTANCE.deserialize(config.getString("format-infos." + format)));
        }
    }

    @Override
    public void lifecycleHandler(SoccerIsAwesomePlugin.ICommandRegistration register) {
        register.register(Commands.literal("shiarecustomname")
                .requires(css -> css.getSender() instanceof Player player && itemCustomNameRenameDialog.isAllowedToOpen(player))
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    if (player.getInventory().getItemInMainHand().isEmpty()) {
                        player.sendMessage(noItemInMainHand);
                        return Command.SINGLE_SUCCESS;
                    }
                    itemCustomNameRenameDialog.open(player);
                    return Command.SINGLE_SUCCESS;
                }).then(Commands.argument("name", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        Player player = (Player) ctx.getSource().getSender();
                        String input = StringArgumentType.getString(ctx, "name");
                        var item = player.getInventory().getItemInMainHand();
                        if (item.isEmpty()) {
                            player.sendMessage(noItemInMainHand);
                            return Command.SINGLE_SUCCESS;
                        }
                        ItemCustomNameRenameDialog.setInItem(player, input, item);
                        item.editPersistentDataContainer(pdc -> {
                            ItemCustomNameRenameDialog.setInPDC(player, pdc, input);
                        });
                        return Command.SINGLE_SUCCESS;
                    })
                )
                .build(),
            "Allows renaming of an Items Name",
            List.of("rename", "setname", "setcustomname")
        );
        register.register(Commands.literal("shiarelore")
                .requires(css -> css.getSender() instanceof Player player && itemLoreRenameDialog.isAllowedToOpen(player))
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    if (player.getInventory().getItemInMainHand().isEmpty()) {
                        player.sendMessage(noItemInMainHand);
                        return Command.SINGLE_SUCCESS;
                    }
                    itemLoreRenameDialog.open(player);
                    return Command.SINGLE_SUCCESS;
                })
                .build(),
            "Allows changing an Items Lore",
            List.of("relore", "setlore")
        );
        register.register(Commands.literal("reitemname")
                .requires(css -> css.getSender() instanceof Player player && itemNameRenameDialog.isAllowedToOpen(player))
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    if (player.getInventory().getItemInMainHand().isEmpty()) {
                        player.sendMessage(noItemInMainHand);
                        return Command.SINGLE_SUCCESS;
                    }
                    itemNameRenameDialog.open(player);
                    return Command.SINGLE_SUCCESS;
                }).then(Commands.argument("name", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        Player player = (Player) ctx.getSource().getSender();
                        String input = StringArgumentType.getString(ctx, "name");
                        var item = player.getInventory().getItemInMainHand();
                        if (item.isEmpty()) {
                            player.sendMessage(noItemInMainHand);
                            return Command.SINGLE_SUCCESS;
                        }
                        ItemNameRenameDialog.setInItem(player, input, item);
                        item.editPersistentDataContainer(pdc -> {
                            ItemNameRenameDialog.setInPDC(player, pdc, input);
                        });
                        return Command.SINGLE_SUCCESS;
                    })
                )
                .build(),
            "Allows changing an Items Lore",
            List.of("setitemname")
        );
        register.register(Commands.literal("shiarename")
                .requires(css -> css.getSender() instanceof Player player && mainRenameDialog.dialogFactories().anyMatch(factory -> factory.isAllowedToOpen(player)))
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    if (player.getInventory().getItemInMainHand().isEmpty()) {
                        player.sendMessage(noItemInMainHand);
                        return Command.SINGLE_SUCCESS;
                    }
                    mainRenameDialog.open(player);
                    return Command.SINGLE_SUCCESS;
                })
                .build(),
            "Allows using various admin commands for items",
            List.of("sr"));
    }


    public static Permission createPermission(@SuppressWarnings("UnstableApiUsage") DataComponentType dataComponentType) {
        final String permissionName = "shia.rename.component." + dataComponentType.key().asString().replace(":", ".");
        @Nullable var perm = Bukkit.getPluginManager().getPermission(permissionName);
        if (perm != null) return perm;
        perm = new Permission(permissionName, "Allows you to add a %s component".formatted(dataComponentType.key().asMinimalString()), PermissionDefault.OP);
        Bukkit.getServer().getPluginManager().addPermission(perm);
        return perm;
    }

    public static final JoinConfiguration COMPONENT_JOIN_FORMAT = JoinConfiguration.builder().separator(Component.text(", ")).lastSeparator(Component.text(" and ")).build();
    public static final LinkedHashMap<String, Component> FORMAT_INFOS = new LinkedHashMap<>();

    public static Component availableFormatsFor(Player player) {
        var formats = FORMAT_INFOS.entrySet().stream()
            .filter(entry -> entry.getKey().equals("reset") || player.hasPermission("shia.rename.format." + entry.getKey()))
            .map(Map.Entry::getValue)
            .toList();
        return Component.join(COMPONENT_JOIN_FORMAT, formats);
    }

    private static void appendIfExists(TextComponent.Builder builder, String format, String def) {
        if (FORMAT_INFOS.containsKey(format)) {
            builder.append(FORMAT_INFOS.get(format));
        } else {
            builder.append(MiniMsgLegacyHybridSerializer.INSTANCE.deserialize(def));
        }
    }
}
