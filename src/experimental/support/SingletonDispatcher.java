package experimental.support;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SingletonDispatcher {

	public static List<Consumer<Action>> wrap(Consumer<Action> dispatcher){
		List<Consumer<Action>> singletonDispatcher = new ArrayList<>();
		singletonDispatcher.add(dispatcher);
		return singletonDispatcher;
	}

}
