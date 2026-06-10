package at.iamsoccer.soccerisawesome.itemrename.dialog.component.copy;

import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractButtonListDialog;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.AbstractDialogButtonFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.AbstractDialogFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.DialogButton;
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
        public boolean isAllowedToOpen(Player player) {
            return CopyComponentsDisplayDialog.this.isAllowedToOpen(player);
        }

        @Override
        protected boolean tryOpen(Player player) {
            return CopyComponentsDisplayDialog.this.tryOpen(player);
        }

        @Override
        protected DialogButton.IButtonInfoSupplier<Player> buttonInfoSupplier(String name) {
            return this::openButtonInfo;
        }

        private DialogButton.ButtonInfo openButtonInfo(Player player) {
            return new DialogButton.ButtonInfo(
                Component.text("Copy " + dataComponentType.key().asMinimalString()), null
            );
        }
    }

    private final Map<DataComponentType, CopyComponentButton> buttons =
        RegistryAccess.registryAccess().getRegistry(RegistryKey.DATA_COMPONENT_TYPE)
            .stream().collect(Collectors.toMap(type -> type, CopyComponentButton::new));

    public CopyComponentsDisplayDialog(Permission permission, @Nullable Supplier<AbstractDialogFactory<Player>> returnDialogFactorySupplier) {
        super(permission, returnDialogFactorySupplier);
    }

    @Override
    public void reload(YamlFileConfig configFile, ConfigurationSection configSection) {
        super.reload(configFile, configSection);
    }

    @Override
    protected List<ActionButton> getDialogButtons(Player player) {
        var mainHand = player.getInventory().getItemInMainHand();
        var offHand = player.getInventory().getItemInOffHand();
        var components = getComponents(offHand);
        var different = components
            .stream().filter(component ->
                !mainHand.hasData(component)
                || component instanceof DataComponentType.Valued valued && !mainHand.getData(valued).equals(offHand.getData(valued))
            ).map(buttons::get)
            .map(button -> button.externalButton().button(player))
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
    protected boolean tryOpen(Player player) {
        if (!super.tryOpen(player)) return false;
        if (player.getInventory().getItemInOffHand().isEmpty() || getDialogButtons(player).isEmpty()) {
            returnToPrevious(player);
            return false;
        }
        return true;
    }
}
