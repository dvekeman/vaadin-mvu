package mvu.sample;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.vaadin.data.Binder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import mvu.sample.model.Person;
import mvu.support.Action;
import mvu.support.ModelViewBinder;
import mvu.support.extra.BoundGrid;
import mvu.support.extra.BoundLabel;
import mvu.support.extra.DispatchButton;

/**
 * Basic Component template
 */
class HerosGrid {

	private HerosGrid() {
	}

	/* ************************************************************************************************************** */
	/* MODEL
	/* ************************************************************************************************************** */

	static class Model {

		final List<Person> heros;
		final String status;
		final Model.Builder builder;

		ThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(5);

		private Model(Model.Builder builder) {
			this.builder = builder;
			this.heros = builder.heros;
			this.status = builder.status;
		}

		static Model.Builder builder() {
			return new Model.Builder();
		}

		static Model copy(Model oldModel, Model.Builder builder) {
			Model newModel = new Model(builder);
			newModel.threadPoolExecutor = oldModel.threadPoolExecutor;
			return newModel;
		}

		static Model initialModel() {
			return builder().build();
		}

		static class Builder {
			List<Person> heros = new ArrayList<>();
			String status = "";

			Model.Builder withHeros(List<Person> heros) {
				this.heros = heros;
				return this;
			}

			Model.Builder withStatus(String status) {
				this.status = status;
				return this;
			}

			Model build() {
				return new Model(this);
			}

		}

	}

	/* ************************************************************************************************************** */
	/* VIEW
	/* ************************************************************************************************************** */

	static Component view(Consumer<Action> mainUpdater) {
		return ModelViewBinder.bindModelAndView(mainUpdater, Model.initialModel(), HerosGrid::view, HerosGrid::update);
	}


	private static Component view(Binder<Model> binder, List<Consumer<Action>> dispatchers) {
		VerticalLayout layout = new VerticalLayout();

		Button loadHeros = DispatchButton.builder(dispatchers)
				.withCaption("Load heros")
				.withAction(LoadHerosAction::new)
				.build();

		Grid<Person> herosGrid = BoundGrid.builder(binder, Person.class)
				.withValueProvider(model -> model.heros)
				.withValueProcessor(Function.identity())
				.build();

		Label statusLabel = BoundLabel.builder(binder, String.class)
				.withValueProvider(model -> model.status)
				.withValueProcessor(Function.identity())
				.build();

		layout.addComponent(loadHeros);
		layout.addComponent(herosGrid);
		layout.addComponent(statusLabel);

		return layout;
	}

	/* ************************************************************************************************************** */
	/* UPDATE
	/* ************************************************************************************************************** */

	private static class LoadHerosAction implements Action {
		LoadHerosAction() {
		}
	}

	private static Model update(Action action, Model oldModel) {
		if (action instanceof LoadHerosAction) {
			Future<List<Person>> futureHeros = fetchHeros(oldModel.threadPoolExecutor);

			try {
				List<Person> heros = futureHeros.get(10, TimeUnit.SECONDS);
				return Model.copy(oldModel, oldModel.builder
						.withHeros(heros)
						.withStatus("Success")
				);

			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				return Model.copy(oldModel, oldModel.builder
						.withHeros(new ArrayList<>())
						.withStatus(e.getMessage())
				);
			}

		} else {
			return oldModel;
		}
	}

	private static Future<List<Person>> fetchHeros(ThreadPoolExecutor threadPoolExecutor) {
		return threadPoolExecutor.submit(() -> {

			URL url = new URL("http://127.0.0.1:8080/vaadin-mvu-example2/rest/persons" /*NO TRAILING SLASH!*/);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");

			if (con.getResponseCode() != 200) {
				throw new ExecutionException(String.format("Fetch failed with status '%s' and message\n%s", con.getResponseCode(), "TOOD"), null);
			}

			String result;
			try (Reader streamReader = new InputStreamReader(con.getInputStream())) {
				try (BufferedReader in = new BufferedReader(streamReader)) {
					String inputLine;
					StringBuilder content = new StringBuilder();
					while ((inputLine = in.readLine()) != null) {
						content.append(inputLine);
					}
					result = content.toString();
				}
			}

			return Arrays.stream(result.split(","))
					.map(s -> s.split(" "))
					.map(personArray -> new Person(personArray[0], personArray[1]))
					.collect(Collectors.toList());
		});
	}

}
