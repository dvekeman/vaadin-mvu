package mvu.sample;

import java.util.List;
import java.util.function.Consumer;

import com.vaadin.data.Binder;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import mvu.sample.model.Person;
import mvu.support.Action;
import mvu.support.ModelViewBinder;
import mvu.support.SingletonDispatcher;
import mvu.support.extra.BoundLabel;
import mvu.support.extra.DispatchButton;

class Main {

	// MODEL
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

	// VIEW
	// This initial bindModelAndView
	static Component view() {
		MainModel initialModel = MainModel.builder().build();
		return ModelViewBinder.bindModelAndView(initialModel, Main::view, Main::update);
	}

	private static Component view(Binder<MainModel> binder, Consumer<Action> dispatcher) {
		HorizontalLayout layout = new HorizontalLayout();

		layout.addComponent(HerosGrid.view(dispatcher));

		return layout;

	}

	private static MainModel update(Action action, MainModel oldModel) {
		return oldModel;
	}
}
