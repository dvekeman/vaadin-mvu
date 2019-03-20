package mvu.support.extra;

import java.util.function.Consumer;
import java.util.function.Function;

import com.vaadin.data.Binder;
import com.vaadin.data.ReadOnlyHasValue;
import com.vaadin.data.ValueProvider;
import com.vaadin.ui.Label;


public class BoundLabel<MODEL, TYPE> {

	private final Label label;

	private BoundLabel(Builder<MODEL, TYPE> builder) {
		this.label = builder.label;

		if (builder.valueProcessor == null) {
			throw new RuntimeException("Please provide a valueProcessor for this label to actually show something. See `withValueProcessor`");
		}

		if (builder.valueProvider== null) {
			throw new RuntimeException("Please provide a valueProvider for this label to actually show something. See `withValueProvider`");
		}

		ReadOnlyHasValue<TYPE> readOnlyLabel;
		if (builder.emptyValue == null) {
			readOnlyLabel = new ReadOnlyHasValue<>(v -> label.setValue(builder.valueProcessor.apply(v)));
		} else {
			readOnlyLabel = new ReadOnlyHasValue<>(v -> label.setValue(builder.valueProcessor.apply(v)), builder.emptyValue);
		}
		builder.binder
				.forField(readOnlyLabel).bind(builder.valueProvider, (model, v) -> {
			throw new UnsupportedOperationException("label fields should not update the model directly!");
		});

	}

	public static <MODEL, TYPE> Builder<MODEL, TYPE> builder(Binder<MODEL> binder, Class<TYPE> labelTypeClass) {
		return new Builder<>(binder, labelTypeClass);
	}

	public static <MODEL, TYPE> Builder<MODEL, TYPE> builder(Label label, Binder<MODEL> binder, Class<TYPE> labelTypeClass) {
		return new Builder<>(label, binder, labelTypeClass);
	}

	public static class Builder<MODEL, TYPE> {

		private final Binder<MODEL> binder;
		private final Label label;
		private final Class<TYPE> labelTypeClass;

		private ValueProvider<MODEL, TYPE> valueProvider;
		private Function<TYPE, String> valueProcessor;
		private TYPE emptyValue;

		private Builder(Binder<MODEL> binder, Class<TYPE> labelTypeClass) {
			this(new Label(), binder, labelTypeClass);
		}

		private Builder(Label label, Binder<MODEL> binder, Class<TYPE> labelTypeClass) {
			this.label = label;
			this.binder = binder;
			this.labelTypeClass = labelTypeClass;
		}


		public Builder<MODEL, TYPE> withValueProvider(ValueProvider<MODEL, TYPE> valueProvider) {
			this.valueProvider = valueProvider;
			return this;
		}

		public Builder<MODEL, TYPE> withValueProcessor(Function<TYPE, String> valueProcessor) {
			this.valueProcessor = valueProcessor;
			return this;
		}

		public Builder<MODEL, TYPE> withEmptyValue(TYPE emptyValue) {
			this.emptyValue = emptyValue;
			return this;
		}

		public Builder<MODEL, TYPE> forLabel(Consumer<Label> consumer) {
			consumer.accept(label);
			return this;
		}

		public Label build() {
			BoundLabel<MODEL, TYPE> boundLabel = new BoundLabel<>(this);
			return boundLabel.label;
		}

	}

}
