package experimental.sample;

import java.util.List;
import java.util.function.Consumer;

import com.vaadin.data.Binder;
import com.vaadin.data.ValueProvider;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;

import experimental.IntegerValidator;
import experimental.support.Action;
import experimental.support.ModelViewBinder;

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
		Model initialModel = Model.builder()
				.build();
		return ModelViewBinder.view(mainUpdater, initialModel, PlusXLayout::view, PlusXLayout::update);
	}


	private static Component view(Binder<Model> binder, List<Consumer<Action>> updaters) {
		HorizontalLayout layout = new HorizontalLayout();

		TextField increment = new TextField();
		binder.forField(increment)
				.withValidator(new IntegerValidator())
				.bind((ValueProvider<Model, String>) model ->
						Integer.toString(model.increment), (model, s) ->
						updaters.forEach(updater ->
								updater.accept(new SetIncrement(Integer.valueOf(s)))));
		layout.addComponent(increment);

		Button plus = new Button(" + ");
		plus.addClickListener(clickEvent ->
				updaters.forEach(updater ->
						updater.accept(new Main.PlusXAction(binder.getBean().increment)))
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


}
