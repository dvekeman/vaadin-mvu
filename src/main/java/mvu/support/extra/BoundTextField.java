package mvu.support.extra;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Function;

import com.vaadin.data.Binder;
import com.vaadin.data.ValueProvider;
import com.vaadin.ui.TextField;

import mvu.support.Action;
import mvu.support.BroadcastAction;
import mvu.support.Dispatcher;

public class BoundTextField<MODEL> implements Serializable {

	private final TextField textField;

	private BoundTextField(Builder<MODEL> builder) {

		this.textField = builder.wrappedField == null ? new TextField() : builder.wrappedField;
		Binder<MODEL> binder = builder.binder;

		if (builder.dispatcher == null) {
			throw new RuntimeException("Missing dispatchers. See `withDispatchers`");
		}

		binder.forField(textField).bind(builder.valueProvider, (model, s) -> {
			Action action = builder.valueConsumer.apply(s);
			if (action instanceof BroadcastAction) {
				builder.dispatcher.getAllDispatchers().forEach(dispatcher ->
						dispatcher.accept(action));
			} else {
				builder.dispatcher.getDispatcher().accept(action);
			}
		});


	}

	public static <MODEL> Builder<MODEL> builder(Binder<MODEL> binder) {
		return new Builder<>(binder);
	}

	public static <MODEL> Builder<MODEL> builder(Binder<MODEL> binder, TextField textField) {
		return new Builder<>(binder, textField);
	}

	public static class Builder<MODEL> implements Serializable {

		private final TextField wrappedField;
		private final Binder<MODEL> binder;
		private ValueProvider<MODEL, String> valueProvider;
		private Function<String, Action> valueConsumer;
		private Dispatcher dispatcher;

		private Builder(Binder<MODEL> binder) {
			this(binder, new TextField());

		}

		private Builder(Binder<MODEL> binder, TextField textField) {
			this.binder = binder;
			this.wrappedField = textField;
		}

		public Builder<MODEL> withDispatcher(Dispatcher dispatcher) {
			this.dispatcher = dispatcher;
			return this;
		}

		public Builder<MODEL> withValueProvider(ValueProvider<MODEL, String> valueProvider) {
			this.valueProvider = valueProvider;
			return this;
		}

		public Builder<MODEL> withValueConsumer(Function<String, Action> valueConsumer) {
			this.valueConsumer = valueConsumer;
			return this;
		}

		public Builder<MODEL> forBinder(Consumer<Binder<MODEL>> withBinder) {
			withBinder.accept(binder);
			return this;
		}

		public Builder<MODEL> forField(Consumer<TextField> withField) {
			withField.accept(wrappedField);
			return this;
		}

		public TextField build() {
			BoundTextField<MODEL> boundTextField = new BoundTextField<>(this);
			return boundTextField.textField;
		}

	}

}
