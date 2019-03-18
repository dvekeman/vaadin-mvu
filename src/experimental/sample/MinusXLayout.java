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

			Model.Builder withDecrement(int decrement) {
				this.decrement = decrement;
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
		return ModelViewBinder.view(mainUpdater, initialModel, MinusXLayout::view, MinusXLayout::update);
	}


	private static Component view(Binder<Model> binder, List<Consumer<Action>> updaters) {
		HorizontalLayout layout = new HorizontalLayout();

		TextField decrement = new TextField();
		binder.forField(decrement)
				.withValidator(new IntegerValidator())
				.bind((ValueProvider<Model, String>) model ->
						Integer.toString(model.decrement), (model, s) ->
						updaters.forEach(updater ->
								updater.accept(new SetDecrement(Integer.valueOf(s)))));
		layout.addComponent(decrement);

		Button plus = new Button(" + ");
		plus.addClickListener(clickEvent ->
				updaters.forEach(updater ->
						updater.accept(new Main.MinusXAction(binder.getBean().decrement)))
		);
		plus.addStyleName("btn-mono");
		layout.addComponent(plus);

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


}
