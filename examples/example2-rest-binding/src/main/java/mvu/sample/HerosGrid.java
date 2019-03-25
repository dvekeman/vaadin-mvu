package mvu.sample;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;

import com.vaadin.data.Binder;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import mvu.sample.model.Person;
import mvu.support.Action;
import mvu.support.BroadcastAction;
import mvu.support.Dispatcher;
import mvu.support.ModelViewBinderKt;
import mvu.support.extra.BoundGrid;
import mvu.support.extra.BoundLabel;

/**
 * Basic Component template
 */
class HerosGrid {

	private HerosGrid() {
	}

	/* ************************************************************************************************************** */
	/* MODEL
	/* ************************************************************************************************************** */

	static class Model {

		final List<Person> heros;
		final String status;
		final Model.Builder builder;

		ThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(5);

		private Model(Model.Builder builder) {
			this.builder = builder;
			this.heros = builder.heros;
			this.status = builder.status;
		}

		static Model.Builder builder() {
			return new Model.Builder();
		}

		static Model copy(Model oldModel, Model.Builder builder) {
			Model newModel = new Model(builder);
			newModel.threadPoolExecutor = oldModel.threadPoolExecutor;
			return newModel;
		}

		static Model initialModel() {
			return builder()
					.build();
		}

		static class Builder {
			List<Person> heros = new ArrayList<>();
			String status = "";

			Model.Builder withHeros(List<Person> heros) {
				this.heros = heros;
				return this;
			}

			Model.Builder withStatus(String status) {
				this.status = status;
				return this;
			}

			Model build() {
				return new Model(this);
			}

		}

	}

	/* ************************************************************************************************************** */
	/* VIEW
	/* ************************************************************************************************************** */

	static Component view(Dispatcher mainUpdater) {
		return ModelViewBinderKt.bindModelAndView(mainUpdater, Model.initialModel(), HerosGrid::view, HerosGrid::update);
	}


	private static Component view(Binder<Model> binder, Dispatcher dispatcher) {
		VerticalLayout layout = new VerticalLayout();

		Label info = new Label();
		info.setContentMode(ContentMode.HTML);
		info.setValue(
				"Clicking the 'Load heros' button will fetch some remote data. </br>"
						+ "There is '2 second' sleep in the server side code.<br/>"
						+ "<br/>"
						+ "After three times, the server will simulate an 'Load Failed' error.<br/>"
						+ "<br/>"
		);

		Component loadBar = LoadBar.view(dispatcher);

		Grid<Person> herosGrid = BoundGrid.builder(binder, Person.class)
				.withValueProvider(model -> model.heros)
				.withValueProcessor(Function.identity())
				.build();

		Label statusLabel = BoundLabel.builder(binder, String.class)
				.withValueProvider(model -> model.status)
				.withValueProcessor(Function.identity())
				.forLabel(label -> label.setContentMode(ContentMode.HTML))
				.build();

		layout.addComponent(info);
		layout.addComponent(loadBar);
		layout.addComponent(herosGrid);
		layout.addComponent(statusLabel);

		return layout;
	}

	/* ************************************************************************************************************** */
	/* UPDATE
	/* ************************************************************************************************************** */

	static class HerosLoading implements BroadcastAction {

	}

	static class HerosLoaded implements BroadcastAction {
		private final List<Person> heros;

		HerosLoaded(List<Person> heros) {
			this.heros = heros;
		}
	}

	static class LoadError implements BroadcastAction {
		private final String error;

		LoadError(String error) {
			this.error = error;
		}
	}

	private static Model update(Action action, Model oldModel) {
		if (action instanceof HerosLoaded) {
			return Model.copy(oldModel, oldModel.builder
					.withStatus("Loaded")
					.withHeros(((HerosLoaded) action).heros)
			);
		} else if (action instanceof LoadError) {
			return Model.copy(oldModel, oldModel.builder
					.withStatus("Loading heros failed: " + ((LoadError) action).error)
			);
		} else if (action instanceof HerosLoading) {
			return Model.copy(oldModel, oldModel.builder
					.withStatus("Loading...")
					.withHeros(new ArrayList<>())
			);
		} else {
			return oldModel;
		}
	}

}
