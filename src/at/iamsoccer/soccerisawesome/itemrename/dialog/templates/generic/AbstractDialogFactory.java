package at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractDialogFactory<User extends Audience> extends AbstractDialogButtonFactory<User> {
    private final DialogButton<User> closeButton = newButton("close", (response, audience) -> {
        returnToPrevious(audience);
    });

    protected AbstractDialogFactory(Class<User> userClass, @Nullable Supplier<AbstractDialogFactory<User>> returnDialogFactorySupplier) {
        super(userClass, returnDialogFactorySupplier);
    }

    @Override
    protected final void onExternalButtonPressed(DialogResponseView response, User user) {
        open(user);
    }

    /**
     * Opens the Dialog specified by this Factory
     *
     * @param user the user to show the dialog to
     */
    public final void open(User user) {
        open(user, null);
    }

    /**
     * Opens the dialog specified by this factory with retaining the response values
     *
     * @param response the response to reincorporate in the input fields
     * @param user     the user to open the dialog for
     */
    protected void open(User user, @Nullable DialogResponseView response) {
        user.showDialog(Dialog.create(builderFactory -> builderFactory.empty()
            .base(DialogBase.builder(dialogTitle(user, response))
                .body(dialogBody(user, response))
                .inputs(dialogInputs(user, response))
                .pause(false)
                .canCloseWithEscape(canCloseWithEscape(user, response))
                .afterAction(hasReturnDialog() ? DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE : DialogBase.DialogAfterAction.CLOSE)
                .build()
            ).type(dialogType(closeButton, user, response))
        ));
    }

    /**
     * The title to use for this Dialog
     *
     * @return the title component
     */
    protected abstract Component dialogTitle(User user, @Nullable DialogResponseView response);

    /**
     * The dialog body, i.e. information fields
     *
     * @param user     the user that will see this body
     * @param response the optional response view, in case of a refresh
     * @return the body list
     */
    protected abstract List<DialogBody> dialogBody(User user, @Nullable DialogResponseView response);
    /**
     * The dialog inputs, i.e. user input fields
     *
     * @param user     the user that will see these input fields
     * @param response the optional response view, in case of a refresh
     * @return the input list
     */
    protected abstract List<DialogInput> dialogInputs(User user, @Nullable DialogResponseView response);

    /**
     * Whether or not you can close this dialog with the ESC key
     *
     * @param user     the user who will open this dialog
     * @param response the optional response view, in case of a refresh
     * @return true if you can close it with ESC key or false if not
     */
    protected boolean canCloseWithEscape(User user, @Nullable DialogResponseView response) {
        return true;
    }

    /**
     * The Dialog Type for this dialog
     *
     * @param closeButton the predefined close button
     * @param user        the user who will open this dialog
     * @param response    the optional response view, in case of a refresh
     * @return the Dialog Type
     */
    protected abstract DialogType dialogType(DialogButton<User> closeButton, User user, @Nullable DialogResponseView response);
}
