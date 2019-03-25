package mvu.support.template;

import static mvu.support.ModelViewBinderKt.bindModelAndView;

import com.vaadin.data.Binder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;

import mvu.support.Action;
import mvu.support.Dispatcher;
import mvu.support.extra.BoundTextField;
import mvu.support.extra.DispatchButton;

/**
 * Basic Component template
 */
class ComponentTemplateJava {

	private ComponentTemplateJava(){}

	static class Model {

		// *Only* use final variables
		// *No* Getters and Setters
		// *Use* the builder
		// >>
		final int value;
		final Model.Builder builder;
		// <<

		private Model(Model.Builder builder) {
			this.builder = builder;
			this.value = builder.value;
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
			int value = 10;

			Model.Builder withValue(int value) {
				this.value = value;
				return this;
			}

			Model build() {
				return new Model(this);
			}

		}

	}

	static Component view(Dispatcher parentDispatcher) {
		return bindModelAndView(parentDispatcher, Model.initialModel(), ComponentTemplateJava::view, ComponentTemplateJava::update);
	}


	private static Component view(Binder<Model> binder, Dispatcher dispatcher) {
		HorizontalLayout layout = new HorizontalLayout();

		TextField textField = BoundTextField.builder(binder)
				// ...
				.build();
		layout.addComponent(textField);

		Button button = DispatchButton.builder(dispatcher)
				// ...
				.build();
		layout.addComponent(button);

		return layout;
	}

	private static class SetValue {
		final int value;

		SetValue(int value) {
			this.value = value;
		}
	}

	private static Model update(Action action, Model oldModel) {
		if (action instanceof SetValue) {
			return Model.copy(oldModel.builder
					.withValue(((SetValue) action).value)
			);
		} else {
			return oldModel;
		}
	}


}
