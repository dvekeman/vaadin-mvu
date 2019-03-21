package mvu.sample;

import java.util.function.Consumer;
import java.util.function.Function;

import com.vaadin.data.Binder;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import mvu.support.Action;
import mvu.support.ModelViewBinder;
import mvu.support.extra.BoundLabel;

class Main {

	static class MainModel {

		final Builder builder;

		final String displayValue;

		private MainModel(Builder builder) {
			this.builder = builder;
			this.displayValue = builder.displayValue;
		}

		static Builder builder() {
			return new Builder();
		}

		static MainModel copy(Builder builder) {
			return new MainModel(builder);
		}

		static MainModel initialModel() {
			return builder()
					.build();
		}

		static class Builder {

			String displayValue;

			Builder withDisplayValue(String displayValue) {
				this.displayValue = displayValue;
				return this;
			}

			MainModel build() {
				return new MainModel(this);
			}

		}

	}


	// VIEW
	// This initial bindModelAndView
	static Component view() {
		return ModelViewBinder.bindModelAndView(MainModel.initialModel(), Main::view, Main::update);
	}

	private static Component view(Binder<MainModel> binder, Consumer<Action> dispatcher) {
		VerticalLayout layout = new VerticalLayout();

		Label mainResult = BoundLabel.builder(binder, String.class)
				.withValueProcessor(Function.identity())
				.withValueProvider(mainModel -> mainModel.displayValue)
				.withEmptyValue("... use the controls below to see this label change ...")
				.build();

		HorizontalLayout controls = new HorizontalLayout();
		Component calculator = Calculator.view(dispatcher);
		Component counter = Counter.view(dispatcher);

		controls.addComponent(counter);
		controls.setComponentAlignment(counter, Alignment.MIDDLE_LEFT);
		controls.addComponent(calculator);
		controls.setComponentAlignment(calculator, Alignment.MIDDLE_RIGHT);

		layout.addComponent(mainResult);
		layout.setComponentAlignment(mainResult, Alignment.MIDDLE_CENTER);
		layout.addComponent(controls);
		layout.setComponentAlignment(controls, Alignment.MIDDLE_CENTER);

		return layout;

	}

	static class SetMainDisplay implements Action {
		final String value;

		SetMainDisplay(String value) {
			this.value = value;
		}
	}

	private static MainModel update(Action action, MainModel oldModel) {
		if (action instanceof SetMainDisplay) {
			return MainModel.copy(oldModel.builder
					.withDisplayValue(((SetMainDisplay) action).value)
			);
		}

		return oldModel;
	}
}
