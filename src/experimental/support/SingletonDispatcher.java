package experimental.support;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Utility class to turn a single dispatcher into a singleton list.
 */
public class SingletonDispatcher {

	/**
	 * Wrap a single dispatcher into a singleton list.
	 *
	 * @param dispatcher A dispatcher
	 * @return A singleton list containing the dispatcher
	 */
	public static List<Consumer<Action>> wrap(Consumer<Action> dispatcher){
		List<Consumer<Action>> singletonDispatcher = new ArrayList<>();
		singletonDispatcher.add(dispatcher);
		return singletonDispatcher;
	}

}
