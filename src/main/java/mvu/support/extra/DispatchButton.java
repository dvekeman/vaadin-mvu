package mvu.support.extra;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.vaadin.ui.Button;

import mvu.support.Action;
import mvu.support.Dispatcher;

public class DispatchButton implements Serializable {

	private final Button button;

	private DispatchButton(Builder builder) {
		button = builder.button;

		if (builder.caption != null) {
			button.setCaption(builder.caption);
		}

		if (builder.actionSupplier == null) {
			throw new RuntimeException("An action supplier must be provided for this button to actually do something");
		}

		button.addClickListener(clickEvent ->
			builder.dispatcher.getDispatcher().invoke((builder.actionSupplier.get()))
		);

	}

	public static DispatchButton.Builder builder(Dispatcher dispatcher) {
		return new DispatchButton.Builder(dispatcher);
	}

	public static DispatchButton.Builder builder(Button button, Dispatcher dispatcher) {
		return new DispatchButton.Builder(button, dispatcher);
	}

	public static class Builder implements Serializable {

		private final Button button;
		private final Dispatcher dispatcher;

		private String caption;
		private Supplier<Action> actionSupplier;


		private Builder(Dispatcher dispatcher) {
			this(new Button(), dispatcher);
		}

		private Builder(Button button, Dispatcher dispatcher) {
			this.button = button;
			this.dispatcher = dispatcher;
		}

		public Builder withCaption(String caption) {
			this.caption = caption;
			return this;
		}

		public Builder forButton(Consumer<Button> buttonConsumer) {
			buttonConsumer.accept(button);
			return this;
		}

		public Builder withAction(Supplier<Action> actionSupplier) {
			this.actionSupplier = actionSupplier;
			return this;
		}

		public Button build() {
			DispatchButton dispatchButton = new DispatchButton(this);
			return dispatchButton.button;
		}

	}

}
