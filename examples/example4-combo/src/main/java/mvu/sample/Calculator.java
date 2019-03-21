package mvu.sample;

import static mvu.sample.Calculator.Operation.RESET;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import com.vaadin.data.Binder;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import mvu.support.Action;
import mvu.support.ComposableDispatcher;
import mvu.support.ModelViewBinder;
import mvu.support.extra.BoundLabel;
import mvu.support.extra.DispatchButton;

class Calculator {

	// Useful for debugging calculator logic :-)
	private static final boolean DEBUG = false;

	enum Operation {
		PLUS("+"), MINUS("-"), MULTIPLY("*"), DIVIDE("/"), RESET("C"), EXEC("=");

		final String caption;

		Operation(String caption) {
			this.caption = caption;
		}
	}

	/* ************************************************************************************************************** */
	/* MODEL
	/* ************************************************************************************************************** */

	static class MainModel {

		final double displayValue;
		final double total;
		final double current;
		final Operation lastOperation;
		final Builder builder;

		private MainModel(Builder builder) {
			this.builder = builder;
			this.displayValue = builder.displayValue;
			this.current = builder.current;
			this.total = builder.total;
			this.lastOperation = builder.lastOperation;
		}

		static Builder builder() {
			return new Builder();
		}

		static MainModel copy(Builder builder) {
			return new MainModel(builder);
		}

		static MainModel initialModel() {
			return builder()
					.withLastOperation(RESET)
					.build();
		}

		static class Builder {
			double displayValue = 0.0;
			double total = 0.0;
			double current;
			Operation lastOperation;

			Builder withDisplayValue(double displayValue) {
				this.displayValue = displayValue;
				return this;
			}

			Builder withTotal(double total) {
				this.total = total;
				return this;
			}

			Builder withCurrent(double current) {
				this.current = current;
				return this;
			}

			Builder withLastOperation(Operation operation) {
				this.lastOperation = operation;
				return this;
			}

			MainModel build() {
				return new MainModel(this);
			}

		}

	}


	/* ************************************************************************************************************** */
	/* VIEW
	/* ************************************************************************************************************** */

	static Component view(Consumer<Action> parentDispatcher) {
		return ModelViewBinder.bindModelAndView(
				MainModel.initialModel(),
				(binder, actionConsumer) ->
						view(binder, parentDispatcher, ComposableDispatcher.compose(parentDispatcher, actionConsumer)),
				Calculator::update);
	}

	private static Component view(Binder<MainModel> binder, Consumer<Action> parentDispatcher, List<Consumer<Action>> dispatchers) {
		VerticalLayout layout = new VerticalLayout();

		GridLayout gridLayout = new GridLayout(4, 5);

		Label display = BoundLabel.builder(binder, String.class)
				.withValueProcessor(Function.identity())
				.withValueProvider(Calculator::toDisplay)
				.withEmptyValue("0.0")
				.build();

		// Create a result label that spans over all the 4 columns in the first row
		gridLayout.addComponent(display, 0, 0, 3, 0);

		// The operations for the calculator in the order they appear on the screen (left to right, top to bottom)
		Action[] operations = new Action[]{
				// 7 8 9 /
				// 4 5 6 *
				// 1 2 3 -
				// 0 = C +
				new NumberAction(7.0), new NumberAction(8), new NumberAction(9), new ArithmeticOperationAction(Operation.DIVIDE),
				new NumberAction(4), new NumberAction(5), new NumberAction(6), new ArithmeticOperationAction(Operation.MULTIPLY),
				new NumberAction(1), new NumberAction(2), new NumberAction(3), new ArithmeticOperationAction(Operation.MINUS),
				new NumberAction(0), new ArithmeticOperationAction(Operation.EXEC), new ArithmeticOperationAction(RESET), new ArithmeticOperationAction(Operation.PLUS)};

		for (Action calcAction : operations) {
			Button operation = DispatchButton
					.builder(dispatchers)
					.withCaption(((WithCaption) calcAction).getCaption())
					.withAction(() -> calcAction)
					.forButton(button -> button.setWidth(45, Sizeable.Unit.PIXELS))
					.build();
			gridLayout.addComponent(operation);
		}

		layout.addComponent(gridLayout);

		Label debugCurrent = BoundLabel.builder(binder, String.class)
				.withValueProcessor(Objects::toString)
				.withValueProvider(mainModel -> "Current: " + mainModel.current)
				.withEmptyValue("Current: ''")
				.build();
		Label debugTotal = BoundLabel.builder(binder, String.class)
				.withValueProcessor(Objects::toString)
				.withValueProvider(mainModel -> "Total: " + mainModel.total)
				.withEmptyValue("Total: ''")
				.build();

		if (DEBUG) {
			layout.addComponent(debugCurrent);
			layout.addComponent(debugTotal);
		}

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
						String.format("Calculator says %s", Double.valueOf(binder.getBean().displayValue).toString()))
				)
		);

		return layout;

	}


	/* ************************************************************************************************************** */
	/* UPDATE
	/* ************************************************************************************************************** */

	interface WithCaption {
		String getCaption();
	}

	static class ArithmeticOperationAction implements Action, WithCaption {
		final String caption;
		final Operation operation;

		ArithmeticOperationAction(Operation operation) {
			this.caption = operation.caption;
			this.operation = operation;
		}

		@Override
		public String getCaption() {
			return caption;
		}
	}

	static class NumberAction implements Action, WithCaption {
		final String caption;
		final double number;

		NumberAction(double number) {
			this.caption = String.format("%s", Double.valueOf(number).intValue());
			this.number = number;
		}

		@Override
		public String getCaption() {
			return caption;
		}
	}


	private static MainModel update(Action action, MainModel oldModel) {
		MainModel.Builder builder = oldModel.builder;

		// Either the user is entering a number
		if (action instanceof NumberAction) {

			double number = ((NumberAction) action).number;
			// In case the user types 1 and then 0 we should have '10' as the current displayValue
			double newCurrent = oldModel.current * 10 + number;

			return MainModel.copy(builder
					.withCurrent(newCurrent)
					.withDisplayValue(newCurrent)
			);

		} else if (action instanceof ArithmeticOperationAction) {
			ArithmeticOperationAction arithmeticOperationAction = (ArithmeticOperationAction) action;

			// Or the user performs an operation
			// In which case we calculate (and store) the intermediate result as newTotal
			double newTotal = doCalculate(oldModel.lastOperation, oldModel.total, oldModel.current);
			builder
					// Save the last operation:
					// PLUS / MINUS / MULTIPLE / DIVIDE or
					// EXEC / RESET
					.withLastOperation(arithmeticOperationAction.operation)
					.withTotal(newTotal)
					.withCurrent(0.0);

			if (Operation.EXEC.equals(arithmeticOperationAction.operation)) {
				// Upon enter we move the total to the display
				return MainModel.copy(builder
						.withDisplayValue(newTotal)
				);
			} else if (RESET.equals(arithmeticOperationAction.operation)) {
				// Upon 'reset' we reset the display and the total value (current is already set to 0.0)
				return MainModel.copy(builder
						.withDisplayValue(0.0)
						.withTotal(0.0)
				);
			} else {
				// Case for PLUS / MINUS / MULTIPLY / DIVIDE
				return MainModel.copy(builder);
			}


		} else {
			return oldModel;
		}
	}

	private static double doCalculate(Operation lastOperation, double oldTotal, double newCurrent) {
		switch (lastOperation) {
			case PLUS:
				return oldTotal + newCurrent;
			case MINUS:
				return oldTotal - newCurrent;
			case DIVIDE:
				return oldTotal / newCurrent;
			case MULTIPLY:
				return oldTotal * newCurrent;
			case EXEC:
				return oldTotal;
			default:
				return newCurrent;
		}
	}

	private static String toDisplay(MainModel model) {
		// Allows us to have a better display (e.g. show the last operator...)
		return String.format("%s", model.displayValue);
	}
}
