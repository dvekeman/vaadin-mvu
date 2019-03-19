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

class PlusX {

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

		static Model initialModel() {
			return builder().build();
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
		return ModelViewBinder.bindModelAndView(mainUpdater, Model.initialModel(), PlusX::view, PlusX::update);
	}

	private static Component view(Binder<PlusX.Model> binder, List<Consumer<Action>> dispatchers) {
		HorizontalLayout layout = new HorizontalLayout();

		TextField increment = BoundTextField.builder(binder)
				.withValueProvider(model -> Integer.toString(model.increment))
				.withValueConsumer(s -> new PlusX.SetIncrement(Integer.valueOf((String) s)))
				.withDispatchers(dispatchers)
				.forField(textField -> textField.addStyleName("SomeStyle"))
				.forField(textField -> textField.setWidth(50, Sizeable.Unit.PIXELS))
				.build();
		layout.addComponent(increment);

		Button plus = DispatchButton.builder(dispatchers)
				.withCaption("+")
				.withAction(() -> new Main.PlusXAction(binder.getBean().increment))
				.forButton(button -> button.addStyleName("btn-mono"))
				.forButton(button -> button.setWidth(50, Sizeable.Unit.PIXELS))
				.build();
		layout.addComponent(plus);

		return layout;
	}

	private static class SetIncrement implements Action {
		final int increment;

		SetIncrement(int increment) {
			this.increment = increment;
		}
	}

	private static Model update(Action action, Model oldModel) {
		if (action instanceof SetIncrement) {
			return Model.copy(oldModel.builder
					.withIncrement(((SetIncrement) action).increment)
			);
		} else {
			return oldModel;
		}
	}


}
