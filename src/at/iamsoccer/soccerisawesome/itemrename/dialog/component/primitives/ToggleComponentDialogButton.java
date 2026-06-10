package at.iamsoccer.soccerisawesome.itemrename.dialog.component.primitives;

import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.itemrename.ItemRenameModule;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.AbstractItemDialogButtonFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.AbstractDialogFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.DialogButton;
import io.papermc.paper.dialog.DialogResponseView;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public abstract class ToggleComponentDialogButton<DataComponentType extends io.papermc.paper.datacomponent.DataComponentType> extends AbstractItemDialogButtonFactory {
    protected final DataComponentType dataComponentType;

    private DialogButton.UnparsedButtonInfo<Player> addInfo;
    private DialogButton.UnparsedButtonInfo<Player> removeInfo;

    public ToggleComponentDialogButton(
        DataComponentType dataComponentType,
        @Nullable Supplier<AbstractDialogFactory<Player>> returnDialogFactorySupplier
    ) {
        super(ItemRenameModule.createPermission(dataComponentType), returnDialogFactorySupplier);
        this.dataComponentType = dataComponentType;
    }

    @Override
    protected void onExternalButtonPressed(DialogResponseView response, Player player) {
        var item = player.getInventory().getItemInMainHand();
        if (shouldAdd(item)) addData(item);
        else removeData(item);
        returnToPrevious(player);
    }

    protected abstract void addData(ItemStack item);
    protected abstract void removeData(ItemStack item);

    public void reload(YamlFileConfig configFile, ConfigurationSection configSection) {
        super.reload(configFile, configSection);
        addInfo = DialogButton.parseFromConfigSection(configFile, configSection, "add-title", null);
        removeInfo = DialogButton.parseFromConfigSection(configFile, configSection, "remove-title", null);
    }

    protected DialogButton.ButtonInfo openButtonInfo(Player player) {
        var item = player.getInventory().getItemInMainHand();
        if (shouldAdd(item)) return addInfo.parse(this, player, null);
        return removeInfo.parse(this, player, null);
    }

    @Override
    public TagResolver tagResolver(Player player, @Nullable DialogResponseView response) {
        return TagResolver.builder()
            .resolver(super.tagResolver(player, response))
            .tag("component", Tag.preProcessParsed(dataComponentType.key().asMinimalString()))
            .build();
    }

    @Override
    protected DialogButton.IButtonInfoSupplier<Player> buttonInfoSupplier(String name) {
        switch (name) {
            case "external": return this::openButtonInfo;
        }
        return super.buttonInfoSupplier(name);
    }

    protected boolean shouldAdd(ItemStack item) {
        return !item.hasData(dataComponentType);
    }
}
