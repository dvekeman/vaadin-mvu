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

import mvu.support.Action;
import mvu.support.ComposableDispatcher;
import mvu.support.ModelViewBinder;
import mvu.support.extra.BoundLabel;
import mvu.support.extra.DispatchButton;

class Counter {

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
	static Component view(Consumer<Action> parentDispatcher) {
		MainModel initialModel = MainModel.builder().build();
		return ModelViewBinder.bindModelAndView(initialModel,
				(binder, actionConsumer) ->
						view(binder, parentDispatcher, ComposableDispatcher.compose(parentDispatcher, actionConsumer)),
				Counter::update);
	}

	private static Component view(Binder<MainModel> binder, Consumer<Action> parentDispatcher, List<Consumer<Action>> dispatchers) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

		// the ticker label itself
		Label tickerLabel = BoundLabel.builder(binder, Integer.class)
				.withValueProcessor(Object::toString)
				.withValueProvider(model -> model.ticker)
				.withEmptyValue(0)
				.forLabel(label -> label.setWidth(50, Sizeable.Unit.PIXELS))
				.build();

		Button plus = DispatchButton.builder(dispatchers)
				.withCaption("+")
				.withAction(PlusAction::new)
				.forButton(button -> button.addStyleName("btn-mono"))
				.forButton(button -> button.setWidth(50, Sizeable.Unit.PIXELS))
				.build();

		Button minus = DispatchButton.builder(dispatchers)
				.withCaption("-")
				.withAction(MinusAction::new)
				.forButton(button -> button.addStyleName("btn-mono"))
				.forButton(button -> button.setWidth(50, Sizeable.Unit.PIXELS))
				.build();

		// the button ( + <x> ) (+) (-) and ( - <x> )
		VerticalLayout buttonLayout = new VerticalLayout();
		buttonLayout.addComponent(PlusX.view(dispatchers));
		buttonLayout.addComponent(plus);
		buttonLayout.setComponentAlignment(plus, Alignment.MIDDLE_RIGHT);
		buttonLayout.addComponent(minus);
		buttonLayout.setComponentAlignment(minus, Alignment.MIDDLE_RIGHT);
		buttonLayout.addComponent(MinusX.view(dispatchers));

		layout.addComponent(tickerLabel);
		layout.addComponent(buttonLayout);

		// This bubbles the value up to the main view
		// This is a limitation (and ugly) because the update function
		// does not support triggering new actions. Remember the signature
		// update : Action -> Model -> Model (vaadin-mvu)
		// vs
		// update : Action -> Model -> (Model, Cmd Action) (The Elm Architecture)
		//
		// Hopefully that can be fixed in version 0.2
		binder.addStatusChangeListener(event ->
				parentDispatcher.accept(new Main.SetMainDisplay(
						String.format("Counter says %s", Integer.valueOf(binder.getBean().ticker).toString()))
				)
		);

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
