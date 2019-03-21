package mvu.support;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Utility class to compose two dispatchers
 */
public class ComposableDispatcher {

	/**
	 * Wrap a single dispatcher into a singleton list.
	 *
	 * @param dispatcher1 The first dispatcher
	 * @param dispatcher2 The second dispatcher
	 * @return A list containing the both dispatchers
	 */
	public static List<Consumer<Action>> compose(Consumer<Action> dispatcher1, Consumer<Action> dispatcher2) {
		List<Consumer<Action>> dispatchers = new ArrayList<>();
		dispatchers.add(dispatcher1);
		dispatchers.add(dispatcher2);
		return dispatchers;
	}

	/**
	 * Add a dispatcher to a list of dispatchers
	 *
	 * @param dispatchers The list of dispatchers
	 * @param dispatcher2 The second dispatcher
	 * @return A list containing the all dispatchers from the first list and the extra dispatcher
	 */
	public static List<Consumer<Action>> compose(List<Consumer<Action>> dispatchers, Consumer<Action> dispatcher2) {
		List<Consumer<Action>> newDispatchers = new ArrayList<>(dispatchers);
		dispatchers.add(dispatcher2);
		return newDispatchers;
	}

	/**
	 * Merge two lists of dispatchers
	 *
	 * @param dispatchers1 The first list of dispatchers
	 * @param dispatchers2 The second list of dispatchers
	 * @return A list containing the all dispatchers
	 */
	public static List<Consumer<Action>> compose(List<Consumer<Action>> dispatchers1, List<Consumer<Action>> dispatchers2) {
		List<Consumer<Action>> newDispatchers = new ArrayList<>(dispatchers1);
		dispatchers1.addAll(dispatchers2);
		return newDispatchers;
	}

}
