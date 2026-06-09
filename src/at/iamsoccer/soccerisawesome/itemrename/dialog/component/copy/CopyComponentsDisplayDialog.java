package at.iamsoccer.soccerisawesome.itemrename.dialog.component.copy;

import at.hugob.plugin.library.config.ConfigUtils;
import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractButtonListDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractDialogButtonFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractDialogFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.buttons.DialogButton;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.dialog.ActionButton;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class CopyComponentsDisplayDialog extends AbstractButtonListDialog {
    private class CopyComponentButton extends AbstractDialogButtonFactory<Player> {
        private final DataComponentType dataComponentType;

        protected CopyComponentButton(DataComponentType dataComponentType) {
            super(Player.class, () -> CopyComponentsDisplayDialog.this);
            this.dataComponentType = dataComponentType;
        }

        @Override
        protected void onExternalButtonPressed(DialogResponseView response, Player player) {
            player.getInventory().getItemInMainHand().copyDataFrom(player.getInventory().getItemInOffHand(), comp -> comp.equals(dataComponentType));
            returnToPrevious(player);
        }

        @Override
        protected boolean isAllowedToOpenInternal(Player player) {
            return CopyComponentsDisplayDialog.this.isAllowedToOpenInternal(player);
        }

        @Override
        protected boolean tryToOpenInternal(Player player) {
            var res = !player.getInventory().getItemInMainHand().isEmpty() && !player.getInventory().getItemInOffHand().isEmpty();
            if (!res) returnToPrevious(player);
            return res;
        }

        @Override
        protected @Nullable DialogButton.ButtonInfo openButtonInfo(Player player) {
            return new DialogButton.ButtonInfo(
                Component.text("Copy " + dataComponentType.key().asMinimalString()), null
            );
        }
    }

    private final Map<DataComponentType, CopyComponentButton> buttons =
        RegistryAccess.registryAccess().getRegistry(RegistryKey.DATA_COMPONENT_TYPE)
            .stream().collect(Collectors.toMap(type -> type, CopyComponentButton::new));

    private DialogButton.ButtonInfo noOffHandItemInfo = new DialogButton.ButtonInfo(Component.empty(), null);

    public CopyComponentsDisplayDialog(Permission permission, @Nullable Supplier<AbstractDialogFactory<Player>> returnDialogFactorySupplier) {
        super(permission, returnDialogFactorySupplier);
    }

    @Override
    public void reload(YamlFileConfig configFile, ConfigurationSection configSection) {
        super.reload(configFile, configSection);
    }

    @Override
    protected void onExternalButtonPressed(DialogResponseView response, Player player) {
        if (player.getInventory().getItemInOffHand().isEmpty()) {
            returnToPrevious(player);
            return;
        }
        super.onExternalButtonPressed(response, player);
    }

    @Override
    protected List<ActionButton> getDialogButtons(Player player, ItemStack item) {
        var offHand = player.getInventory().getItemInOffHand();
        var components = getComponents(offHand);
        var different = components
            .stream().filter(component ->
                !item.hasData(component)
                || component instanceof DataComponentType.Valued valued && !item.getData(valued).equals(offHand.getData(valued))
            ).map(buttons::get)
            .map(button -> button.openActionButton(player))
            .toList();
        return different;
    }

    private Set<DataComponentType> getComponents(ItemStack itemStack) {
        var set = new HashSet<DataComponentType>();
        set.addAll(itemStack.getDataTypes());
        set.addAll(itemStack.getType().asItemType().getDefaultDataTypes());
        return set;
    }

    @Override
    protected boolean tryToOpenInternal(Player player) {
        var val = super.tryToOpenInternal(player);
        if (!val) return false;
        val = !player.getInventory().getItemInOffHand().isEmpty() && !getDialogButtons(player, player.getInventory().getItemInMainHand()).isEmpty();
        if (!val) {
            returnToPrevious(player);
        }
        return val;
    }

    @Override
    protected @Nullable DialogButton.ButtonInfo openButtonInfo(Player player) {
        if (player.getInventory().getItemInOffHand().isEmpty()) {
            return noOffHandItemInfo;
        }
        return super.openButtonInfo(player);
    }
}
