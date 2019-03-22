package mvu.support;

/**
 *
 * @param <LEFT> Typically the onFail action
 * @param <RIGHT> Typically the onSucceed action
 */
public class AsyncActionResult<LEFT, RIGHT> {

	private final LEFT left;
	private final RIGHT right;

	private AsyncActionResult(LEFT left, RIGHT right) {
		this.left = left;
		this.right = right;
	}

	public LEFT left() {
		if (this.left == null && this.right != null) {
			throw new RuntimeException("This AsyncActionResult has a Right value, first assert that this AsyncActionResult isLeft before invoking the left operation.");
		}
		return this.left;
	}

	public RIGHT right() {
		if (this.right == null && this.left != null) {
			throw new RuntimeException("This AsyncActionResult has a Left value, first assert that this AsyncActionResult isRight before invoking the right operation.");
		}
		return this.right;
	}

	public boolean isLeft() {
		return this.left != null && this.right == null;
	}

	public boolean isRight() {
		return this.right != null && this.left == null;
	}


	public static <LEFT, RIGHT> AsyncActionResult<LEFT, RIGHT> fromLeft(LEFT left, Class<RIGHT> rightClass) {
		return new AsyncActionResult<>(left, null);
	}

	public static <LEFT, RIGHT> AsyncActionResult<LEFT, RIGHT> fromRight(RIGHT right, Class<LEFT> leftClass) {
		return new AsyncActionResult<>(null, right);
	}
}
