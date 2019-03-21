package mvu.support.extra;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.crypto.Data;

import com.vaadin.data.Binder;
import com.vaadin.data.HasValue;
import com.vaadin.data.ReadOnlyHasValue;
import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.shared.Registration;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;

import mvu.sample.model.Person;

public class BoundGrid<MODEL, T> {

	private final HorizontalLayout gridLayout = new HorizontalLayout();
	private final Grid<T> grid;

	private BoundGrid(Builder<MODEL, T> builder) {
		this.grid = builder.grid;

		if (builder.valueProvider == null) {
			throw new RuntimeException("Please provide a valueProvider for this grid to actually show something. See `withValueProvider`");
		}

		builder.binder.forField(new HasValue<Collection<T>>() {

			private List<ValueChangeListener<Collection<T>>> valueChangeListeners = new ArrayList<>();

			@Override
			public void setValue(Collection<T> value) {
				grid.setDataProvider(new ListDataProvider<>(value));
			}

			@Override
			public Collection<T> getValue() {
				DataProvider<T, ?> dataProvider = grid.getDataProvider();
				if (dataProvider instanceof ListDataProvider) {
					return ((ListDataProvider<T>) dataProvider).getItems();
				} else {
					throw new RuntimeException("Cannot get the value from a non-list grid dataprovider");
				}
			}

			@Override
			public Registration addValueChangeListener(ValueChangeListener<Collection<T>> listener) {
				valueChangeListeners.add(listener);
				return (Registration) () -> valueChangeListeners.remove(listener);
			}

			@Override
			public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {

			}

			@Override
			public boolean isRequiredIndicatorVisible() {
				return false;
			}

			@Override
			public void setReadOnly(boolean readOnly) {

			}

			@Override
			public boolean isReadOnly() {
				return false;
			}
		}).bind(
				model -> {
					Collection<T> items = builder.valueProvider.apply(model);
					grid.getDataProvider().refreshAll();
					return items;
				},
				(model, v) -> {
					throw new UnsupportedOperationException("grids should not update the model directly!");
				});


	}

	public static <MODEL, T> Builder<MODEL, T> builder(Binder<MODEL> binder, Class<T> gridTypeClass) {
		return new Builder<>(binder, gridTypeClass);
	}

	public static <MODEL, T> Builder<MODEL, T> builder(Grid<T> grid, Binder<MODEL> binder) {
		return new Builder<>(grid, binder, grid.getBeanType());
	}

	public static class Builder<MODEL, T> {

		private final Binder<MODEL> binder;
		private final Grid<T> grid;
		private final Class<T> gridTypeClass;

		private Collection<T> initialItems;
		private ValueProvider<MODEL, Collection<T>> valueProvider;
		private Function<Collection<T>, Collection<T>> valueProcessor = Function.identity();

		public Builder<MODEL, T> withValueProvider(ValueProvider<MODEL, Collection<T>> valueProvider) {
			this.valueProvider = valueProvider;
			return this;
		}

		public Builder<MODEL, T> withValueProcessor(Function<Collection<T>, Collection<T>> valueProcessor) {
			this.valueProcessor = valueProcessor;
			return this;
		}

		private Builder(Binder<MODEL> binder, Class<T> gridTypeClass) {
			this(new Grid<>(gridTypeClass), binder, gridTypeClass);
		}

		private Builder(Grid<T> grid, Binder<MODEL> binder, Class<T> gridTypeClass) {
			this.grid = grid;
			this.binder = binder;
			this.gridTypeClass = gridTypeClass;
		}

		public Builder<MODEL, T> withInitialItems(Collection<T> initialItems) {
			this.initialItems = initialItems;
			return this;
		}

		public Grid<T> build() {
			BoundGrid<MODEL, T> boundGrid = new BoundGrid<>(this);
			return boundGrid.grid;
		}

	}

}
