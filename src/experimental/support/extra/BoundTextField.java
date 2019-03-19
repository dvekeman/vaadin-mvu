package experimental.support.extra;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.vaadin.data.Binder;
import com.vaadin.data.ValueProvider;
import com.vaadin.ui.TextField;

import experimental.support.Action;

public class BoundTextField<MODEL> {

	private final TextField textField;

	private BoundTextField(Builder<MODEL> builder) {

		this.textField = builder.wrappedField == null ? new TextField() : builder.wrappedField;
		Binder<MODEL> binder = builder.binder;

		if(builder.dispatchers == null){
			throw new RuntimeException("Missing dispatchers. See `withDispatchers`");
		}

		binder.forField(textField).bind(builder.valueProvider, (model, s) -> {
			List<Consumer<Action>> updaters = builder.dispatchers;
			Action action = builder.valueConsumer.apply(s);
			updaters.forEach(updater -> updater.accept(action));
		});


	}

	public static <MODEL> Builder<MODEL> builder(Binder<MODEL> binder) {
		return new Builder<>(binder);
	}

	public static <MODEL> Builder<MODEL> builder(Binder<MODEL> binder, TextField textField) {
		return new Builder<>(binder, textField);
	}

	public static class Builder<MODEL> {

		private final TextField wrappedField;
		private final Binder<MODEL> binder;
		private ValueProvider<MODEL, String> valueProvider;
		private Function<String, Action> valueConsumer;
		private List<Consumer<Action>> dispatchers;

		private Builder(Binder<MODEL> binder) {
			this(binder, new TextField());

		}

		private Builder(Binder<MODEL> binder, TextField textField) {
			this.binder = binder;
			this.wrappedField = textField;
		}

		public Builder<MODEL> withDispatchers(List<Consumer<Action>> dispatchers) {
			this.dispatchers = dispatchers;
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
