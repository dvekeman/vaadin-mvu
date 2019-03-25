package mvu.sample;

import com.vaadin.data.Binder;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

import mvu.support.Action;
import mvu.support.Dispatcher;
import mvu.support.ModelViewBinderKt;

class Main {

	/* ************************************************************************************************************** */
	/* MODEL
	/* ************************************************************************************************************** */

	static class MainModel {

		final Builder builder;

		private MainModel(Builder builder) {
			this.builder = builder;
		}

		static Builder builder() {
			return new Builder();
		}

		static MainModel copy(Builder builder) {
			return new MainModel(builder);
		}

		static class Builder {

			MainModel build() {
				return new MainModel(this);
			}

		}

	}

	/* ************************************************************************************************************** */
	/* VIEW
	/* ************************************************************************************************************** */

	static Component view() {
		MainModel initialModel = MainModel.builder().build();
		return ModelViewBinderKt.bindModelAndView(initialModel, Main::view, Main::update);
	}

	private static Component view(Binder<MainModel> binder, Dispatcher dispatcher) {
		HorizontalLayout layout = new HorizontalLayout();

		layout.addComponent(HerosGrid.view(dispatcher));

		return layout;

	}

	/* ************************************************************************************************************** */
	/* UPDATE
	/* ************************************************************************************************************** */

	private static MainModel update(Action action, MainModel oldModel) {
		return oldModel;
	}
}
