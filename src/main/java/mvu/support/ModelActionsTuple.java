package mvu.support;

import java.util.List;

public class ModelActionsTuple<MODEL> {

	private final MODEL model;
	private final List<Action> actions;

	private ModelActionsTuple(MODEL model, List<Action> actions){
		this.model = model;
		this.actions = actions;
	}

	public MODEL getModel() {
		return model;
	}

	public List<Action> getActions() {
		return actions;
	}

	public static <MODEL> ModelActionsTuple<MODEL> create(MODEL model, List<Action> actions){
		return new ModelActionsTuple<>(model, actions);
	}

}
