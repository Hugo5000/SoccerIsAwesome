package at.iamsoccer.soccerisawesome.itemrename.dialog.templates;

import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.action.DialogAction;

public interface IActionButtonFactory {
    ActionButton openActionButton();
    DialogAction openAction();
}
