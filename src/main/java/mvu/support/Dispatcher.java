package mvu.support;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Dispatcher {
	
	final List<Consumer<Action>> parentDispatchers;
	
	final Consumer<Action> dispatcher;
	
	public Dispatcher(Consumer<Action> dispatcher){
		this(new ArrayList<>(), dispatcher);
	}
	
	public Dispatcher(List<Consumer<Action>> parentDispatchers, Consumer<Action> dispatcher){
		this.parentDispatchers = parentDispatchers;
		this.dispatcher = dispatcher;
	}

	public static Dispatcher empty() {
		return new Dispatcher(new ArrayList<>(), action -> {});
	}

	public List<Consumer<Action>> getParentDispatchers() {
		return parentDispatchers;
	}

	public Consumer<Action> getDispatcher() {
		return dispatcher;
	}

	public List<Consumer<Action>> getAllDispatchers(){
		List<Consumer<Action>> allDispatchers = new ArrayList<>();
		allDispatchers.add(dispatcher);
		allDispatchers.addAll(parentDispatchers);
		return allDispatchers;
	}
}
