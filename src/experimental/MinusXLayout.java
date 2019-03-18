package experimental;

import java.util.function.Consumer;

import com.vaadin.data.Binder;
import com.vaadin.data.ValidationResult;
import com.vaadin.data.Validator;
import com.vaadin.data.ValueProvider;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;

class MinusXLayout {

	static class Model {

		final int decrement;
		final Model.Builder builder;

		private Model(Model.Builder builder) {
			this.builder = builder;
			this.decrement = builder.decrement;
		}

		static Model.Builder builder() {
			return new Model.Builder();
		}

		static Model copy(Model.Builder builder) {
			return new Model(builder);
		}

		static class Builder {
			int decrement = 10;

			Model.Builder withDecrement(int increment) {
				this.decrement = increment;
				return this;
			}

			Model build() {
				return new Model(this);
			}

		}

	}

	static Component view(Consumer<Action> mainUpdater) {
		return view(mainUpdater, Model.builder().build());
	}


	private static Component view(Consumer<Action> mainUpdater, Binder<Model> binder, Consumer<Action> updater) {
		HorizontalLayout layout = new HorizontalLayout();

		TextField decrement = new TextField();
		binder.forField(decrement)
				.withValidator((Validator<String>) (s, valueContext) -> {
					if(s == null || s.isEmpty()){
						return ValidationResult.ok();
					}
					try {
						Integer.valueOf(s);
						return ValidationResult.ok();
					} catch (NumberFormatException nfe) {
						return ValidationResult.error(String.format("Cannot convert %s into an integer", s));
					} catch (Exception e) {
						return ValidationResult.error("Unknown exception converting your input");
					}
				})
				.bind((ValueProvider<Model, String>) model -> Integer.toString(model.decrement), (model, s) -> {
					updater.accept(new SetDecrement(Integer.valueOf(s)));
				});
		layout.addComponent(decrement);

		Button minus = new Button(" - ");
		minus.addClickListener(clickEvent ->
				mainUpdater.accept(new Main.MinusXAction(binder.getBean().decrement))
		);
		minus.addStyleName("btn-mono");
		layout.addComponent(minus);

		return layout;
	}

	private static class SetDecrement implements Action {
		final int decrement;

		SetDecrement(int decrement) {
			this.decrement = decrement;
		}
	}

	private static Model update(Model oldModel, Action action) {
		if (action instanceof SetDecrement) {
			return Model.copy(oldModel.builder
					.withDecrement(((SetDecrement) action).decrement)
			);
		} else {
			throw new RuntimeException(String.format("Action %s is not yet implemented!", action));
		}
	}


	// BOILERPLATE / HELPER
	// These methods could be moved somewhere else to make the code less boilerplate-y

	private static Component view(Consumer<Action> mainUpdater, Model model) {
		Binder<Model> binder = new Binder<>();
		binder.setBean(model);

		return view(mainUpdater, binder, action -> {
			Model oldModel = binder.getBean();
			// This is the important part: update creates a new MainModel and rebinds it.
			Model newModel = update(oldModel, action);
			binder.setBean(newModel);
		});
	}

}
