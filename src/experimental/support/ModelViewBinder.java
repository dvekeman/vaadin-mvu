package experimental.support;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.vaadin.data.Binder;
import com.vaadin.ui.Component;

public class ModelViewBinder {

	/**
	 * Bind the model and the view with a parent dispatcher.
	 *
	 * Parents are notified of all children events but should only act upon the ones they care about.
	 * Typically this should only happen when a child uses a parent action.
	 *
	 * For example: imagine we have a form with a ticker and two child components. One which increments
	 * the ticker with a user specified value and another one which decrements the ticker with a user specified
	 * value.
	 *
	 * The main layout consists of a label with the current value (hence the main model contains the current value)
	 * and the two child components.
	 *
	 * The child components consist of an input field to specify the amount and a button to trigger it.
	 * When clicking on the button in the child component the main layout needs to be notified that the
	 * ticker has to be incremented or decremented by x.
	 *
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
	 *
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
	 *
	 * @param parentDispatcher Will be notified of any changes but should only
	 * @param model Model to be bound to the view
	 * @param view View which renders the bound model
	 * @param update Set of dispatchers which respond to actions.
	 * @param <MODEL> Generic model parameter
	 * @return A Vaadin component
	 */
	public static <MODEL> Component bindModelAndView(
			Consumer<Action> parentDispatcher,
			/* MODEL  */ MODEL model,
			/* VIEW   */ BiFunction<Binder<MODEL>, List<Consumer<Action>>, Component> view,
			/* UPDATE */ BiFunction<Action, MODEL, MODEL> update) {

		Binder<MODEL> binder = new Binder<>();
		binder.setBean(model);

		List<Consumer<Action>> updaters = new ArrayList<>();
		updaters.add(parentDispatcher);
		updaters.add(
				action -> {
					MODEL oldModel = binder.getBean();
					// This is the important part: update creates a new MainModel and rebinds it.
					MODEL newModel = update.apply(action, oldModel);
					binder.setBean(newModel);
				}
		);

		return view.apply(binder, updaters);
	}

	/**
	 * Same as {@link #bindModelAndView(Consumer, Object, BiFunction, BiFunction)} but without the parent dispatcher.
	 *
	 * This is either for the root component which does not have a parent or for child components which do not
	 * interact with their ancesters.
	 *
	 * @param model Model to be bound to the view
	 * @param view View which renders the bound model
	 * @param update Set of dispatchers which respond to actions.
	 * @param <MODEL> Generic model parameter
	 * @return A Vaadin component
	 */
	public static <MODEL> Component bindModelAndView(
			/* MODEL  */ MODEL model,
			/* VIEW   */ BiFunction<Binder<MODEL>, Consumer<Action>, Component> view,
			/* UPDATE */ BiFunction<Action, MODEL, MODEL> update) {

		Binder<MODEL> binder = new Binder<>();
		binder.setBean(model);

		return view.apply(binder, action -> {
			MODEL oldModel = binder.getBean();
			// This is the important part: update creates a new MainModel and rebinds it.
			MODEL newModel = update.apply(action, oldModel);
			binder.setBean(newModel);
		});
	}


}
