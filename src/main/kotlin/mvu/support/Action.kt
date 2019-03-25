package mvu.support

/**
 * Marker interface for Actions which are triggered by a user entering data, clicking a button, etc.
 *
 * When creating your own Actions, just implement this interface and that's it.
 */
interface Action

/**
 *
 * Marker interface for Actions which are broadcasted to all dispatchers
 *
 * Note: When applied to an AsyncAction, this means the async action will run for all dispatchers!
 *
 */
interface BroadcastAction : Action


/**
 * Marker interface for Async Actions such as loading data from a remote backend.
 */
interface AsyncAction<STARTACTION, LEFTACTION, RIGHTACTION> : Action {

    val startAction: STARTACTION

    fun <LEFTACTION, RIGHTACTION> perform(): AsyncActionResult<LEFTACTION, RIGHTACTION>

}

/**
 *
 * @param <LEFT> Typically the onFail action
 * @param <RIGHT> Typically the onSucceed action
 */
class AsyncActionResult<LEFT, RIGHT> internal constructor(private val left: LEFT?, private val right: RIGHT?) {

    val isLeft: Boolean
        get() = this.left != null && this.right == null

    val isRight: Boolean
        get() = this.right != null && this.left == null

    fun left(): LEFT {
        if (this.left == null && this.right != null) {
            throw RuntimeException("This AsyncActionResult has a Right value, first assert that this AsyncActionResult isLeft before invoking the left operation.")
        }
        return this.left!!
    }

    fun right(): RIGHT {
        if (this.right == null && this.left != null) {
            throw RuntimeException("This AsyncActionResult has a Left value, first assert that this AsyncActionResult isRight before invoking the right operation.")
        }
        return this.right!!
    }

}

fun <LEFT, RIGHT> fromLeft(left: LEFT): AsyncActionResult<LEFT, RIGHT> {
    return AsyncActionResult(left = left, right = null)
}

fun <LEFT, RIGHT> fromRight(right: RIGHT): AsyncActionResult<LEFT, RIGHT> {
    return AsyncActionResult(left = null, right = right)
}
