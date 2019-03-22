package mvu.support;

/**
 *
 * Marker interface for Actions which are broadcasted to all dispatchers
 *
 * Note: When applied to an AsyncAction, this means the async action will run for all dispatchers!
 *
 */
public interface BroadcastAction extends Action {}
