package mvu.support;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.vaadin.data.Binder;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.PushConfiguration;
import com.vaadin.ui.UI;

/**
 * The <code>ModelViewBinder</code> is the engine of the Model-View-Update pattern.
 * <p>
 * The bindModelAndView functions take, as arguments, ... the pattern!
 *
 * <ul>
 * <li>
 * A model class (which is a data class)
 * </li>
 * <li>
 * A view function (which renders the model)
 * </li>
 * <li>
 * An update function which acts on Actions, produces a new MODEL and - eventually - causes the view to update
 * </li>
 * </ul>
 */
public class ModelViewBinder {

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
	//TODO: update doc
	public static <MODEL> Component bindModelAndView(
			Dispatcher parentDispatcher,
			/* MODEL  */ MODEL model,
			/* VIEW   */ BiFunction<Binder<MODEL>, Dispatcher, Component> view,
			/* UPDATE */ BiFunction<Action, MODEL, MODEL> update) {

		Binder<MODEL> binder = new Binder<>();
		binder.setBean(model);

		Dispatcher dispatcher = new Dispatcher(parentDispatcher.getAllDispatchers(), action -> {
			MODEL oldModel = binder.getBean();
			// This is the important part: update creates a new MainModel and rebinds it.
			MODEL newModel = update.apply(action, oldModel);
			binder.setBean(newModel);
		});

		return view.apply(binder, dispatcher);

	}

//	public static <MODEL> Component bindModelAndView(
//			Dispatcher parentDispatcher,
//			/* MODEL  */ MODEL model,
//			/* VIEW   */ BiFunction<Binder<MODEL>, Dispatcher, Component> view,
//			/* UPDATE */ BiFunction<Action, MODEL, MODEL> update) {
//		return bindModelAndView(parentDispatcher, model, view, update);
//	}
//

	/**
	 * Same as {@link #bindModelAndView(Dispatcher, Object, BiFunction, BiFunction)} but without the parent dispatcher.
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
	public static <MODEL> Component bindModelAndView(
			/* MODEL  */ MODEL model,
			/* VIEW   */ BiFunction<Binder<MODEL>, Dispatcher, Component> view,
			/* UPDATE */ BiFunction<Action, MODEL, MODEL> update) {

		Binder<MODEL> binder = new Binder<>();
		binder.setBean(model);

		VaadinSession vaadinSession = VaadinSession.getCurrent();
		Dispatcher dispatcher = new Dispatcher(action ->
				doSyncAction(vaadinSession, binder, Dispatcher.empty(), update, action)
		);

		return view.apply(binder, dispatcher);
	}

	public static <MODEL> Component bindModelAndViewV2(
			Dispatcher parentDispatcher,
			/* MODEL  */ MODEL model,
			/* VIEW   */ BiFunction<Binder<MODEL>, Dispatcher, Component> view,
			/* UPDATE */ BiFunction<Action, MODEL, MODEL> update) {

		Binder<MODEL> binder = new Binder<>();
		binder.setBean(model);

		VaadinSession vaadinSession = VaadinSession.getCurrent();
		List<Consumer<Action>> parentDispatchers = parentDispatcher.getAllDispatchers();
		Dispatcher dispatcher = new Dispatcher(parentDispatchers, action ->
				doSyncOrAsyncAction(vaadinSession, binder, update, parentDispatcher, action)
		);

		return view.apply(binder, dispatcher);
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
	private static <MODEL> void doSyncOrAsyncAction(VaadinSession vaadinSession, Binder<MODEL> binder, BiFunction<Action, MODEL, MODEL> update, Dispatcher parentDispatcher, Action action) {
		if (action instanceof AsyncAction) {
			AsyncAction asyncAction = ((AsyncAction)action);
			boolean pushEnabled = vaadinSession.getUIs().stream()
					.map(UI::getPushConfiguration)
					.map(PushConfiguration::getPushMode)
					.anyMatch(PushMode::isEnabled);
			if (!pushEnabled) {
				throw new RuntimeException("Vaadin Push must be enabled for AsyncActions. Enable @Push for this UI.");
			}
			// First run the start action (e.g. set the screen to 'Loading'
			doSyncAction(vaadinSession, binder, parentDispatcher, update, asyncAction.getStartAction());
			// Then run the async task itself
			CompletableFuture<AsyncActionResult<? extends Action, ? extends Action>> f = CompletableFuture.supplyAsync((asyncAction)::perform);
			// And run the action through the dispatcher (typically this is *not* a broadcast action so only the owner component should respond to this
			doSyncAction(vaadinSession, binder, parentDispatcher, update, action);
			// When the result comes back execute either the Fail or the Success action
			f.thenAccept(eitherLeftOrRight -> {
				if (eitherLeftOrRight.isLeft()) {
					doSyncAction(vaadinSession, binder, parentDispatcher, update, eitherLeftOrRight.left());
				} else {
					doSyncAction(vaadinSession, binder, parentDispatcher, update, eitherLeftOrRight.right());
				}
			});
		} else {
			doSyncAction(vaadinSession, binder, parentDispatcher, update, action);
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
	private static <MODEL> void doSyncAction(VaadinSession vaadinSession, Binder<MODEL> binder, Dispatcher parentDispatcher, BiFunction<Action, MODEL, MODEL> update, Action action) {
		MODEL oldModel = binder.getBean();
		MODEL newModel = update.apply(action, oldModel);

		if (action instanceof BroadcastAction) {
			parentDispatcher.getAllDispatchers().forEach(dispatcher ->
					dispatcher.accept(action)
			);
		}

		vaadinSession.getUIs().forEach(ui ->
				ui.access(() -> {
					binder.setBean(newModel);
					ui.push();
				})
		);
	}

}
