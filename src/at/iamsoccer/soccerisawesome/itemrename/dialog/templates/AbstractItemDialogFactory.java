package at.iamsoccer.soccerisawesome.itemrename.dialog.templates;

import at.hugob.plugin.library.config.YamlFileConfig;
import at.iamsoccer.soccerisawesome.itemrename.ItemRenameModule;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.ConfigDialogFactory;
import at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.AbstractDialogFactory;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractItemDialogFactory extends ConfigDialogFactory<Player> {
    private final ConfigDialogFactory<Player> missingItemDialog;
    protected final @Nullable Permission permission;

    public AbstractItemDialogFactory(@Nullable Permission permission, @Nullable Supplier<AbstractDialogFactory<Player>> returnFactorySupplier) {
        super(Player.class, returnFactorySupplier);
        this.missingItemDialog = new ConfigDialogFactory<>(Player.class, returnFactorySupplier);
        this.permission = permission;
    }

    @Override
    public void reload(YamlFileConfig configFile, ConfigurationSection configSection) {
        super.reload(configFile, configSection);
        missingItemDialog.reload(configFile, configFile.getConfigurationSection("dialog.no-item-in-main-hand"));
    }

    @Override
    public boolean isAllowedToOpen(Player player) {
        if (!super.isAllowedToOpen(player)) return false;
        return permission == null || player.hasPermission(permission);
    }

    @Override
    protected boolean tryOpen(Player player) {
        if (!super.tryOpen(player)) return false;
        boolean hasItem = !player.getInventory().getItemInMainHand().isEmpty();
        if (!hasItem) {
            missingItemDialog.open(player);
        }
        return hasItem;
    }

    @Override
    protected List<DialogBody> dialogBody(Player player, @Nullable DialogResponseView response) {
        var body = super.dialogBody(player, response);
        var item = player.getInventory().getItemInMainHand().clone();
        modifyPreview(player, response, item);
        body.add(DialogBody.item(item).build());
        return body;
    }

    protected void modifyPreview(Player player, @Nullable DialogResponseView response, ItemStack item){
    }

    @Override
    public TagResolver tagResolver(Player player, @Nullable DialogResponseView response) {
        return TagResolver.builder()
            .resolver(super.tagResolver(player, response))
            .tag("available_formats", Tag.selfClosingInserting(ItemRenameModule.availableFormatsFor(player, hasSignTag())))
        .build();
    }

    protected boolean hasSignTag() {
        return false;
    }
}
