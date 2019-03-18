package experimental;

import java.util.function.Consumer;

import com.vaadin.data.Binder;
import com.vaadin.data.ReadOnlyHasValue;
import com.vaadin.server.SerializableConsumer;
import com.vaadin.server.Setter;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 *
 * [ ] External API calls (e.g. a REST call)
 *
 *
 */
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
	// This initial view
	static Component view() {
		return view(MainModel.builder().build());
	}

	private static Component view(Binder<MainModel> binder, Consumer<Action> updater) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

		// the ticker label itself
		Label tickerLabel = new Label();
		ReadOnlyHasValue<Integer> tickerLabelValue = new ReadOnlyHasValue<>((SerializableConsumer<Integer>) v ->
				tickerLabel.setValue(Integer.toString(v)), 0);
		binder.forField(tickerLabelValue).bind(model -> model.ticker, (Setter<MainModel, Integer>) (model, integer) -> {
			throw new UnsupportedOperationException("fields should not update the model directly!");
		});
		layout.addComponent(tickerLabel);

		// the button ( + <x> ) (+) (-) and ( - <x> )
		VerticalLayout buttonLayout = new VerticalLayout();

		buttonLayout.addComponent(PlusXLayout.view(updater));

		Button plus = new Button(" + ");
		plus.addClickListener(clickEvent ->
				updater.accept(new PlusAction()));
		plus.addStyleName("btn-mono");

		Button minus = new Button(" - ");
		minus.addClickListener(clickEvent ->
				updater.accept(new MinusAction()));
		minus.addStyleName("btn-mono");
		buttonLayout.addComponents(plus, minus);

		buttonLayout.addComponent(MinusXLayout.view(updater));

		layout.addComponent(buttonLayout);

		return layout;

	}

	private static class PlusAction implements Action {}
	private static class MinusAction implements Action {}
	static class PlusXAction implements Action{
		final int increment;
		PlusXAction(int increment){
			this.increment = increment;
		}
	}
	static class MinusXAction implements Action{
		final int decrement;
		MinusXAction(int decrement){
			this.decrement = decrement;
		}
	}

	private static MainModel update(MainModel oldModel, Action action) {
		if (action instanceof PlusAction) {
			return MainModel.copy(oldModel.builder
					.withTicker(oldModel.ticker + 1)
			);
		} else if (action instanceof MinusAction) {
			return MainModel.copy(oldModel.builder
					.withTicker(oldModel.ticker - 1)
			);
		} else if (action instanceof PlusXAction) {
			int increment = ((PlusXAction)action).increment;
			return MainModel.copy(oldModel.builder
					.withTicker(oldModel.ticker + increment)
			);
		} else if (action instanceof MinusXAction) {
			int decrement = ((MinusXAction)action).decrement;
			return MainModel.copy(oldModel.builder
					.withTicker(oldModel.ticker - decrement)
			);
		} else {
			throw new RuntimeException(String.format("Action %s is not yet implemented!", action));
		}
	}

	// BOILERPLATE / HELPER
	// These methods could be moved somewhere else to make the code less boilerplate-y

	private static Component view(MainModel model) {
		Binder<MainModel> binder = new Binder<>();
		binder.setBean(model);

		return view(binder, action -> {
			MainModel oldModel = binder.getBean();
			// This is the important part: update creates a new MainModel and rebinds it.
			MainModel newModel = update(oldModel, action);
			binder.setBean(newModel);
		});
	}

}
