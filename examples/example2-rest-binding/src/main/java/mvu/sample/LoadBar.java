package mvu.sample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.vaadin.data.Binder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;

import mvu.sample.model.Person;
import mvu.support.Action;
import mvu.support.AsyncAction;
import mvu.support.AsyncActionResult;
import mvu.support.ModelViewBinder;
import mvu.support.extra.BoundTextField;
import mvu.support.extra.DispatchButton;

/**
 * Basic Component template
 */
class LoadBar {

	private LoadBar() {
	}

	/* ************************************************************************************************************** */
	/* MODEL
	/* ************************************************************************************************************** */

	static class Model {

		final String url;
		final Model.Builder builder;

		private Model(Model.Builder builder) {
			this.builder = builder;
			this.url = builder.url;
		}

		static Model.Builder builder() {
			return new Model.Builder();
		}

		static Model copy(Model.Builder builder) {
			return new Model(builder);
		}

		static Model initialModel() {
			return builder()
					.withUrl("http://127.0.0.1:8080/vaadin-mvu-example2")
					.build();
		}

		static class Builder {
			String url = "";

			Model.Builder withUrl(String url) {
				this.url = url;
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

	static Component view(List<Consumer<Action>> mainUpdater) {
		return ModelViewBinder.bindModelAndViewV2(mainUpdater, Model.initialModel(), LoadBar::view, LoadBar::update);
	}


	private static Component view(Binder<Model> binder, List<Consumer<Action>> dispatchers) {
		HorizontalLayout loadLayout = new HorizontalLayout();
		TextField url = BoundTextField.builder(binder)
				.withDispatchers(dispatchers)
				.withValueConsumer(SetUrl::new)
				.withValueProvider(model -> model.url)
				.build();
		Button loadHeros = DispatchButton.builder(dispatchers)
				.withCaption("Load heros")
				.withAction(() -> new LoadHeros(binder.getBean().url))
				.build();

		loadLayout.addComponent(url);
		loadLayout.addComponent(loadHeros);

		return loadLayout;
	}

	/* ************************************************************************************************************** */
	/* UPDATE
	/* ************************************************************************************************************** */

	private static class SetUrl implements Action {
		final String url;

		SetUrl(String url) {
			this.url = url;
		}
	}

	static class LoadHeros implements Action, AsyncAction<HerosGrid.LoadError, HerosGrid.HerosLoaded> {
		final String url;

		LoadHeros(String url) {
			this.url = url;
		}

		@Override
		public AsyncActionResult<HerosGrid.LoadError, HerosGrid.HerosLoaded> perform() {
			return fetchHeros(this.url);
		}
	}

	private static Model update(Action action, Model oldModel) {
		if (action instanceof SetUrl) {
			return Model.copy(oldModel.builder
					.withUrl(((SetUrl) action).url)
			);
		} else {
			return oldModel;
		}
	}

	private static AsyncActionResult<HerosGrid.LoadError, HerosGrid.HerosLoaded> fetchHeros(String currentUrl) {
		try {
			System.err.println(String.format("Loading data from %s", currentUrl + "/rest/persons"));
			URL url = new URL(currentUrl + "/rest/persons" /*NO TRAILING SLASH!*/);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");

			if (con.getResponseCode() != 200) {
				String error = errorToString(con);
				throw new ExecutionException(String.format("Fetch failed with status '%s' and message\n%s", con.getResponseCode(), error), null);
			}

			String result = responseToString(con);

			List<Person> heros = Arrays.stream(result.split(","))
					.map(s -> s.split(" "))
					.map(personArray -> new Person(personArray[0], personArray[1]))
					.collect(Collectors.toList());
			return AsyncActionResult.fromRight(new HerosGrid.HerosLoaded(heros), HerosGrid.LoadError.class);
		} catch (Exception e) {
			e.printStackTrace();
			String errorMsg = String.join("</br>", getErrorMessages(new ArrayList<>(), e));
			return AsyncActionResult.fromLeft(new HerosGrid.LoadError(errorMsg), HerosGrid.HerosLoaded.class);
		}
	}

	private static List<String> getErrorMessages(List<String> messages, Throwable throwable) {
		List<String> newMessages = new ArrayList<>(messages);
		newMessages.add(throwable.getMessage());
		if (throwable.getCause() != null) {
			return getErrorMessages(newMessages, throwable.getCause());
		}
		return newMessages;
	}

	private static String errorToString(HttpURLConnection con) throws IOException {
		return readData(con.getErrorStream());
	}

	private static String responseToString(HttpURLConnection con) throws IOException {
		return readData(con.getInputStream());
	}

	private static String readData(InputStream dataStream) throws IOException {
		String result;
		try (Reader streamReader = new InputStreamReader(dataStream)) {
			try (BufferedReader in = new BufferedReader(streamReader)) {
				String inputLine;
				StringBuilder content = new StringBuilder();
				while ((inputLine = in.readLine()) != null) {
					content.append(inputLine);
				}
				result = content.toString();
			}
		}
		return result;

	}


}
