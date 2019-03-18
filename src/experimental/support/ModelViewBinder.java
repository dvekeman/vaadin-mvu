package experimental.support;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.google.gwt.thirdparty.guava.common.collect.Lists;
import com.vaadin.data.Binder;
import com.vaadin.ui.Component;

public class ModelViewBinder {

	public static <MODEL> Component view(Consumer<Action> mainUpdater, MODEL model, BiFunction<Binder<MODEL>, List<Consumer<Action>>, Component> view, BiFunction<MODEL, Action, MODEL> update) {
		Binder<MODEL> binder = new Binder<>();
		binder.setBean(model);

		List<Consumer<Action>> updaters = Lists.newArrayList();
		updaters.add(mainUpdater);
		updaters.add(
				action -> {
					MODEL oldModel = binder.getBean();
					// This is the important part: update creates a new MainModel and rebinds it.
					MODEL newModel = update.apply(oldModel, action);
					binder.setBean(newModel);
				}
		);

		return view.apply(binder, updaters);
	}

	public static <MODEL> Component view(MODEL model, BiFunction<Binder<MODEL>, Consumer<Action>, Component> view, BiFunction<MODEL, Action, MODEL> update) {
		Binder<MODEL> binder = new Binder<>();
		binder.setBean(model);

		return view.apply(binder, action -> {
			MODEL oldModel = binder.getBean();
			// This is the important part: update creates a new MainModel and rebinds it.
			MODEL newModel = update.apply(oldModel, action);
			binder.setBean(newModel);
		});
	}


}
