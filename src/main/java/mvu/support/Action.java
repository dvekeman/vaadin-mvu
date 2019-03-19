package mvu.support;

// Actions or Messages
// Why no enum? Because some actions might carry custom data (e.g. PlusXAction)

/**
 * Marker interface for Actions which are triggered by a user entering data, clicking a button, etc.
 *
 * When creating your own Actions, just implement this interface and that's it.
 */
public interface Action {}
