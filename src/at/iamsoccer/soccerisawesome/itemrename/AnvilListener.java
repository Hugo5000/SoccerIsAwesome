package at.iamsoccer.soccerisawesome.itemrename;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.AnvilView;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import static at.iamsoccer.soccerisawesome.itemrename.dialog.rename.ItemCustomNameRenameDialog.CUSTOM_NAME_KEY;
import static at.iamsoccer.soccerisawesome.itemrename.dialog.rename.ItemCustomNameRenameDialog.getCustomNameSuggestionFromItem;
import static at.iamsoccer.soccerisawesome.itemrename.dialog.rename.ItemCustomNameRenameDialog.setCustomNameInItem;
import static at.iamsoccer.soccerisawesome.itemrename.dialog.rename.ItemCustomNameRenameDialog.setCustomNameInPDC;
import static net.kyori.adventure.text.Component.text;

public class AnvilListener implements Listener {
    private final static MiniMessage translateableOnlyMiniMessageSerlializer = MiniMessage.builder().tags(TagResolver.resolver(
        StandardTags.translatable(),
        StandardTags.translatableFallback()
    )).build();
    private final static NamespacedKey ANVIL_KEY = NamespacedKey.fromString("rename:anvil");
    private final static NamespacedKey ANVIL_RESULT_KEY = NamespacedKey.fromString("rename:anvil_result");
    private final static NamespacedKey ANVIL_NAME_KEY = NamespacedKey.fromString("rename:anvil_custom");
    public static final String ANVIL_CUSTOM_NAME_PERM = "shia.rename.anvil";

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onAnvil(PrepareAnvilEvent event) {
        if (!(event.getView().getPlayer() instanceof Player player)) return;
        @Nullable var item = event.getResult();
        if (item == null || item.isEmpty()) return;
        var customName = item.getData(DataComponentTypes.CUSTOM_NAME);
        if (customName == null) return;
        if (!player.hasPermission("shia.rename.custom-name.anvil")) return;
        String name = PlainTextComponentSerializer.plainText().serialize(customName);
        setCustomNameInItem(player, name, item);
        item.editPersistentDataContainer(pdc -> {
            setCustomNameInPDC(player, pdc, name);
            pdc.remove(ANVIL_KEY);
            pdc.remove(ANVIL_NAME_KEY);
            pdc.set(ANVIL_RESULT_KEY, PersistentDataType.STRING, name);
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onStartAnvil(PrepareAnvilEvent event) {
        if (!(event.getView().getPlayer() instanceof Player player)) return;
        @Nullable var item = event.getInventory().getFirstItem();
        if (item == null || item.isEmpty() || item.getPersistentDataContainer().has(ANVIL_KEY)) return;
        if (!player.hasPermission(ANVIL_CUSTOM_NAME_PERM)) return;
        var suggestion = getCustomNameSuggestionFromItem(player, item);
        item.editPersistentDataContainer(pdc -> {
            pdc.set(ANVIL_KEY, PersistentDataType.BOOLEAN, true);
            if (item.hasData(DataComponentTypes.CUSTOM_NAME)) {
                pdc.set(ANVIL_NAME_KEY, PersistentDataType.STRING, GsonComponentSerializer.gson().serialize(item.getData(DataComponentTypes.CUSTOM_NAME)));
            }
        });
        item.setData(DataComponentTypes.CUSTOM_NAME, translateableOnlyMiniMessageSerlializer.deserialize(suggestion.suggestion()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onAnvilClose(InventoryCloseEvent event) {
        if (!(event.getView() instanceof AnvilView anvilView)) return;
        var inventory = anvilView.getTopInventory();
        @Nullable var item = inventory.getFirstItem();
        undoAnvilName(item);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onAnvilPutInMainItem(InventoryClickEvent event) {
        if (!(event.getView() instanceof AnvilView anvilView)) return;
        @Nullable var item = event.getCurrentItem();
        undoAnvilName(item);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onAnvilPullOutResultItem(InventoryClickEvent event) {
        if (!(event.getView() instanceof AnvilView anvilView)) return;
        if (!(anvilView.getPlayer() instanceof Player player)) return;
        @Nullable var item = event.getCurrentItem();
        if (item == null || item.isEmpty()) return;
        if (!player.hasPermission("shia.rename.custom-name.anvil")) return;
        if (!item.getPersistentDataContainer().has(ANVIL_RESULT_KEY, PersistentDataType.STRING)) return;
        final String customName = item.getPersistentDataContainer().get(ANVIL_RESULT_KEY, PersistentDataType.STRING);
        setCustomNameInItem(player, customName, item);
        item.editPersistentDataContainer(pdc -> {
            setCustomNameInPDC(player, pdc, customName);
            pdc.remove(ANVIL_RESULT_KEY);
        });
    }

    private static void undoAnvilName(@Nullable ItemStack item) {
        if (item == null || item.isEmpty()) return;
        if (!item.getPersistentDataContainer().has(ANVIL_KEY)) return;
        @Nullable final String customName = item.getPersistentDataContainer().get(ANVIL_NAME_KEY, PersistentDataType.STRING);
        if (customName != null && item.hasData(DataComponentTypes.CUSTOM_NAME)) {
            item.setData(DataComponentTypes.CUSTOM_NAME, GsonComponentSerializer.gson().deserialize(customName));
        } else {
            item.resetData(DataComponentTypes.CUSTOM_NAME);
            item.editPersistentDataContainer(pdc -> pdc.remove(CUSTOM_NAME_KEY));
        }
        item.editPersistentDataContainer(pdc -> {
            pdc.remove(ANVIL_KEY);
            pdc.remove(ANVIL_NAME_KEY);
        });
    }
}
