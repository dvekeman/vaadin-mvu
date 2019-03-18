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

class PlusXLayout {

	static class Model {

		final int increment;
		final Model.Builder builder;

		private Model(Model.Builder builder) {
			this.builder = builder;
			this.increment = builder.increment;
		}

		static Model.Builder builder() {
			return new Model.Builder();
		}

		static Model copy(Model.Builder builder) {
			return new Model(builder);
		}

		static class Builder {
			int increment = 10;

			Model.Builder withIncrement(int increment) {
				this.increment = increment;
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

		TextField increment = new TextField();
		binder.forField(increment)
				.withValidator((Validator<String>) (s, valueContext) -> {
					try {
						if(s == null || s.isEmpty()){
							return ValidationResult.ok();
						}
						Integer.valueOf(s);
						return ValidationResult.ok();
					} catch (NumberFormatException nfe) {
						return ValidationResult.error(String.format("Cannot convert %s into an integer", s));
					} catch (Exception e) {
						return ValidationResult.error("Unknown exception converting your input");
					}
				})
				.bind((ValueProvider<Model, String>) model -> Integer.toString(model.increment), (model, s) -> {
					updater.accept(new SetIncrement(Integer.valueOf(s)));
				});
		layout.addComponent(increment);

		Button plus = new Button(" + ");
		plus.addClickListener(clickEvent ->
				mainUpdater.accept(new Main.PlusXAction(binder.getBean().increment))
		);
		plus.addStyleName("btn-mono");
		layout.addComponent(plus);

		return layout;
	}

	private static class SetIncrement implements Action {
		final int increment;

		SetIncrement(int increment) {
			this.increment = increment;
		}
	}

	private static Model update(Model oldModel, Action action) {
		if (action instanceof SetIncrement) {
			return Model.copy(oldModel.builder
					.withIncrement(((SetIncrement) action).increment)
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
