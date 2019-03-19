package experimental.sample;

import java.util.List;
import java.util.function.Consumer;

import com.vaadin.data.Binder;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;

import experimental.support.Action;
import experimental.support.extra.BoundTextField;
import experimental.support.extra.DispatchButton;
import experimental.support.ModelViewBinder;

class MinusX {

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

		static Model initialModel(){
			return builder().build();
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
		return ModelViewBinder.bindModelAndView(mainUpdater, Model.initialModel(), MinusX::view, MinusX::update);
	}


	private static Component view(Binder<Model> binder, List<Consumer<Action>> dispatchers) {
		HorizontalLayout layout = new HorizontalLayout();

		TextField decrement = BoundTextField.builder(binder)
				.withValueProvider(model -> Integer.toString(model.decrement))
				.withValueConsumer(s -> new SetDecrement(Integer.valueOf((String) s)))
				.withDispatchers(dispatchers)
				.forField(textField ->
						textField.addStyleName("SomeStyle"))
				.forField(textField ->
						textField.setWidth(50, Sizeable.Unit.PIXELS))
				.build();
		layout.addComponent(decrement);

		Button minus = DispatchButton.builder(dispatchers)
				.withCaption("-")
				.withAction(() -> new Main.MinusXAction(binder.getBean().decrement))
				.forButton(button -> button.addStyleName("btn-mono"))
				.forButton(button -> button.setWidth(50, Sizeable.Unit.PIXELS))
				.build();
		layout.addComponent(minus);

		return layout;
	}

	private static class SetDecrement implements Action {
		final int decrement;

		SetDecrement(int decrement) {
			this.decrement = decrement;
		}
	}

	private static Model update(Action action, Model oldModel) {
		if (action instanceof SetDecrement) {
			return Model.copy(oldModel.builder
					.withDecrement(((SetDecrement) action).decrement)
			);
		} else {
			return oldModel;
		}
	}


}
