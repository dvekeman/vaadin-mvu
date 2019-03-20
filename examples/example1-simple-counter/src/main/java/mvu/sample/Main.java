package mvu.sample;

import java.util.function.Consumer;

import com.vaadin.data.Binder;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import mvu.support.Action;
import mvu.support.ModelViewBinder;
import mvu.support.SingletonDispatcher;
import mvu.support.extra.BoundLabel;
import mvu.support.extra.DispatchButton;

class Main {

	// MODEL
	// The MainModel is an immutable POJO
	static class MainModel {

		final int ticker;
		final Builder builder;

		private MainModel(Builder builder) {
			this.builder = builder;
			this.ticker = builder.ticker;
		}

		static Builder builder() {
			return new Builder();
		}

		static MainModel copy(Builder builder) {
			return new MainModel(builder);
		}

		static class Builder {
			int ticker = 0;

			Builder withTicker(int ticker) {
				this.ticker = ticker;
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
		MainModel initialModel = MainModel.builder().build();
		return ModelViewBinder.bindModelAndView(initialModel, Main::view, Main::update);
	}

	private static Component view(Binder<MainModel> binder, Consumer<Action> dispatcher) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

		// the ticker label itself
		Label tickerLabel = BoundLabel.builder(binder, Integer.class)
				.withValueProcessor(Object::toString)
				.withValueProvider(model -> model.ticker)
				.withEmptyValue(0)
				.forLabel(label -> label.setWidth(50, Sizeable.Unit.PIXELS))
				.build();

		Button plus = DispatchButton.builder(SingletonDispatcher.wrap(dispatcher))
				.withCaption("+")
				.withAction(PlusAction::new)
				.forButton(button -> button.addStyleName("btn-mono"))
				.forButton(button -> button.setWidth(50, Sizeable.Unit.PIXELS))
				.build();

		Button minus = DispatchButton.builder(SingletonDispatcher.wrap(dispatcher))
				.withCaption("-")
				.withAction(MinusAction::new)
				.forButton(button -> button.addStyleName("btn-mono"))
				.forButton(button -> button.setWidth(50, Sizeable.Unit.PIXELS))
				.build();

		// the button ( + <x> ) (+) (-) and ( - <x> )
		VerticalLayout buttonLayout = new VerticalLayout();
		buttonLayout.addComponent(PlusX.view(dispatcher));
		buttonLayout.addComponent(plus);
		buttonLayout.setComponentAlignment(plus, Alignment.MIDDLE_RIGHT);
		buttonLayout.addComponent(minus);
		buttonLayout.setComponentAlignment(minus, Alignment.MIDDLE_RIGHT);
		buttonLayout.addComponent(MinusX.view(dispatcher));

		layout.addComponent(tickerLabel);
		layout.addComponent(buttonLayout);

		return layout;

	}

	private static class PlusAction implements Action {}

	private static class MinusAction implements Action {}

	static class PlusXAction implements Action {
		final int increment;

		PlusXAction(int increment) {
			this.increment = increment;
		}
	}

	static class MinusXAction implements Action {
		final int decrement;

		MinusXAction(int decrement) {
			this.decrement = decrement;
		}
	}

	private static MainModel update(Action action, MainModel oldModel) {
		if (action instanceof PlusAction) {
			return MainModel.copy(oldModel.builder
					.withTicker(oldModel.ticker + 1)
			);
		} else if (action instanceof MinusAction) {
			return MainModel.copy(oldModel.builder
					.withTicker(oldModel.ticker - 1)
			);
		} else if (action instanceof PlusXAction) {
			int increment = ((PlusXAction) action).increment;
			return MainModel.copy(oldModel.builder
					.withTicker(oldModel.ticker + increment)
			);
		} else if (action instanceof MinusXAction) {
			int decrement = ((MinusXAction) action).decrement;
			return MainModel.copy(oldModel.builder
					.withTicker(oldModel.ticker - decrement)
			);
		} else {
			return oldModel;
		}
	}
}
