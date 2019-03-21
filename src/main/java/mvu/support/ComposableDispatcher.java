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
	public static List<Consumer<Action>> compose(Consumer<Action> dispatcher1, Consumer<Action> dispatcher2){
		List<Consumer<Action>> dispatchers = new ArrayList<>();
		dispatchers.add(dispatcher1);
		dispatchers.add(dispatcher2);
		return dispatchers;
	}

}
