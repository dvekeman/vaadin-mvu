/**
 * The <code>ModelViewBinder</code> is the engine of the Model-View-Update pattern.
 * <p>
 * The bindModelAndView functions take, as arguments, ... the pattern!
 *
 * <ul>
 *   <li>
 *     A model class (which is a data class)
 *   </li>
 *   <li>
 *     A view function (which renders the model)
 *   </li>
 *   <li>
 *     An update function which acts on Actions, produces a new MODEL and - eventually - causes the view to update
 *   </li>
 * </ul>
 */
package mvu.support

import com.vaadin.data.Binder
import com.vaadin.server.VaadinSession
import com.vaadin.shared.communication.PushMode
import com.vaadin.ui.Component
import com.vaadin.ui.PushConfiguration
import java.util.concurrent.CompletableFuture


/**
 * Kotlin Users: use ViewKt
 * Java Users: use View
 */
typealias ViewKt<MODEL> = (binder: Binder<MODEL>, dispatcher: Dispatcher) -> Component


/**
 * Kotlin Users: use ViewKt
 * Java Users: use View
 */
@FunctionalInterface
interface View<MODEL> {
    fun invoke(binder: Binder<MODEL>, dispatcher: Dispatcher): Component
}


/**
 * Kotlin Users: use UpdateKt
 * Java Users: use Update
 */
typealias UpdateKt<MODEL> = (action: Action, model: MODEL) -> MODEL


/**
 * Kotlin Users: use UpdateKt
 * Java Users: use Update
 */
@FunctionalInterface
interface Update<MODEL> {
    fun invoke(action: Action, model: MODEL): MODEL
}


/**
 * Bind the model and the view with a parent dispatcher.
 * <p>
 * Parents are notified of all children events but should only act upon the ones they care about.
 * Typically this should only happen when a child uses a parent action.
 * <p>
 * For example: imagine we have a form with a ticker and two child components. One which increments
 * the ticker with a user specified value and another one which decrements the ticker with a user specified
 * value.
 * <p>
 * The main layout consists of a label with the current value (hence the main model contains the current value)
 * and the two child components.
 * <p>
 * The child components consist of an input field to specify the amount and a button to trigger it.
 * When clicking on the button in the child component the main layout needs to be notified that the
 * ticker has to be incremented or decremented by x.
 * <p>
 * Below is the code for the button in the child component. Note that it triggers the <code>Main.MinusAction</code>
 * The child component update method is free to act upon this action, e.g. by logging it. But in this case
 * the main component will pick up the action event and apply the decrement to the main ticker state.
 *
 *
 * <strong>MinusXLayout</strong>
 *
 * <pre>
 * plusX.addClickListener(event ->
 *   dispatcher.forEach(updater ->
 * 	   updater.accept(new Main.MinusXAction(binder.getBean().decrement)))
 * );
 * </pre>
 * <p>
 * And here is the code from the Main component:
 *
 * <strong>Main</strong>
 * <pre>
 * private static MainModel update(MainModel oldModel, Action action) {
 * 	 if (action instanceof PlusAction) {
 * 		...
 *   } else if (action instanceof MinusAction) {
 * 		...
 *   } else if (action instanceof PlusXAction) {
 * 		int increment = ((PlusXAction) action).increment;
 * 		return MainModel.copy(oldModel.builder
 * 				.withTicker(oldModel.ticker + increment)
 * 		);
 *   } else if (action instanceof MinusXAction) {
 * 		int decrement = ((MinusXAction) action).decrement;
 * 		return MainModel.copy(oldModel.builder
 * 				.withTicker(oldModel.ticker - decrement)
 * 		);
 *   } else {
 * 		throw new RuntimeException(String.format("Action %s is not yet implemented!", action));
 *   }
 * }
 * </pre>
 *
 * @param parentDispatcher Will be notified of any changes but should only
 * @param model            Model to be bound to the view
 * @param view             View which renders the bound model
 * @param update           Set of dispatchers which respond to actions.
 * @param <MODEL>          Generic model parameter
 * @return A Vaadin component
 */
fun <MODEL> bindModelAndView(parentDispatcher: Dispatcher,
                             model: MODEL,
                             view: View<MODEL>,
                             update: Update<MODEL>): Component {

    val binder = Binder<MODEL>()
    binder.bean = model

    val vaadinSession = VaadinSession.getCurrent()
    val dispatcher = Dispatcher(parentDispatcher.allDispatchers) { action ->
        doSyncOrAsyncAction(vaadinSession, binder, update, parentDispatcher, action)
    }

    return view.invoke(binder, dispatcher)

}


/**
 * Wrapper for Kotlin
 * @see #bindModelAndView(parentDispatcher, model, view, update)
 */
fun <MODEL> bindModelAndViewKt(parentDispatcher: Dispatcher, model: MODEL, view: ViewKt<MODEL>, update: UpdateKt<MODEL>): Component {
    return bindModelAndView(parentDispatcher, model, view2kt(view), update2kt(update))
}


/**
 * Same as {@link #bindModelAndView(Dispatcher, MODEL, View, Update)} but without the parent dispatcher.
 * <p>
 * This is either for the root component which does not have a parent or for child components which do not
 * interact with their ancesters.
 *
 * @param model   Model to be bound to the view
 * @param view    View which renders the bound model
 * @param update  Set of dispatchers which respond to actions.
 * @param <MODEL> Generic model parameter
 * @return A Vaadin component
 */
fun <MODEL> bindModelAndView(model: MODEL, view: View<MODEL>, update: Update<MODEL>): Component {
    return bindModelAndView(emptyDispatcher(), model, view, update)
}


/**
 * Wrapper for Kotlin
 * @see #bindModelAndView(model, view, update)
 */
fun <MODEL> bindModelAndViewKt(model: MODEL, view: ViewKt<MODEL>, update: UpdateKt<MODEL>): Component {
    return bindModelAndView(model, view2kt(view), update2kt(update))
}


/**
 * Sync actions execute immediately, where async actions run in three steps:
 * <ul>
 * <li>Step 1: Start the async part (e.g. a call to a remote backend)</li>
 * <li>Step 2: Run the (initial) action (e.g. mark the view as 'Loading...')</li>
 * <li>Step 3: Upon arrival of the result: run the follow up action</li>
 * </ul>
 * <p>
 * An async follow up action is either a Left (typically 'Fail') or a Right (Typically 'Success') result:
 *
 * <ul>
 * <li>Left | FAIL: Fetching from the remote side failed (e.g. mark a status label as 'Failed...')</li>
 * <li>Right | Succeed: Fetching succeeded, run the action to process the result (e.g. update the UI with the remote data)</li>
 * </ul>
 *
 * @param binder  (Vaadin) UI binder
 * @param update  Set of dispatchers which respond to actions.
 * @param action  Action to run
 * @param <MODEL> Generic model parameter
 */
private fun <MODEL> doSyncOrAsyncAction(
        vaadinSession: VaadinSession,
        binder: Binder<MODEL>,
        update: Update<MODEL>,
        parentDispatcher: Dispatcher,
        action: Action) {
    if (action is AsyncAction<*, *, *>) {
        val pushEnabled = isPushEnabled(vaadinSession)
        if (!pushEnabled) {
            throw RuntimeException("Vaadin Push must be enabled for AsyncActions. Enable @Push for this UI.")
        }
        // First run the start action (e.g. set the screen to 'Loading'
        doSyncAction(vaadinSession, binder, parentDispatcher, update, action.startAction as Action)
        // Then run the async task itself
        // TODO: can we use coroutines here?
        val f: CompletableFuture<AsyncActionResult<out Action, out Action>>  = CompletableFuture.supplyAsync<AsyncActionResult<out Action, out Action>> {
            action.perform()
        }
        // And run the action through the dispatcher (typically this is *not* a broadcast action so only the owner component should respond to this
        doSyncAction(vaadinSession, binder, parentDispatcher, update, action)
        // When the result comes back execute either the Fail or the Success action
        f.thenAccept { eitherLeftOrRight: AsyncActionResult<out Action, out Action> ->
            if (eitherLeftOrRight.isLeft) {
                doSyncAction(vaadinSession, binder, parentDispatcher, update, eitherLeftOrRight.left())
            } else {
                doSyncAction(vaadinSession, binder, parentDispatcher, update, eitherLeftOrRight.right())
            }
        }
    } else {
        doSyncAction(vaadinSession, binder, parentDispatcher, update, action)
    }
}

/**
 * Helper method which runs an action through the dispatchers.
 *
 * @param binder  (Vaadin) UI binder
 * @param update  Set of dispatchers which respond to actions.
 * @param action  Action to run
 * @param <MODEL> Generic model parameter
 */
private fun <MODEL> doSyncAction(vaadinSession: VaadinSession,
                                 binder: Binder<MODEL>,
                                 parentDispatcher: Dispatcher,
                                 update: Update<MODEL>,
                                 action: Action) {
    val oldModel = binder.bean
    val newModel = update.invoke(action, oldModel)

    if (action is BroadcastAction) {
        parentDispatcher.allDispatchers.forEach { dispatcher ->
            dispatcher(action)
        }
    }

    if (isPushEnabled(vaadinSession)) {
        vaadinSession.uIs.forEach { ui ->
            ui.access {
                binder.bean = newModel
                ui.push()
            }
        }
    } else {
        binder.setBean(newModel)
    }
}

private fun isPushEnabled(vaadinSession: VaadinSession): Boolean {
    return vaadinSession.uIs.stream()
            .map<PushConfiguration> { it.pushConfiguration }
            .map<PushMode> { it.pushMode }
            .anyMatch { it.isEnabled }
}


// HELPER

// Kotlin <-> Java Conversion

/**
 * Convert a Kotlin ViewKt type to a Java View type
 */
fun <MODEL> view2kt(viewKt: ViewKt<MODEL>): View<MODEL> {
    return object : View<MODEL> {
        override fun invoke(binder: Binder<MODEL>, dispatcher: Dispatcher): Component {
            return viewKt.invoke(binder, dispatcher)
        }
    }
}

/**
 * Convert a Kotlin UpdateKt type to a Java Update type
 */
fun <MODEL> update2kt(updateKt: UpdateKt<MODEL>): Update<MODEL> {
    return object : Update<MODEL> {
        override fun invoke(action: Action, model: MODEL): MODEL {
            return updateKt.invoke(action, model)
        }
    }
}
