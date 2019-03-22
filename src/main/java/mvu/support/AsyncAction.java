package mvu.support;

/**
 * Marker interface for Async Actions such as loading data from a remote backend.
 */
public interface AsyncAction<STARTACTION extends Action, LEFTACTION extends Action, RIGHTACTION extends Action> extends Action {

	AsyncActionResult<LEFTACTION, RIGHTACTION> perform();

	STARTACTION getStartAction();

}
