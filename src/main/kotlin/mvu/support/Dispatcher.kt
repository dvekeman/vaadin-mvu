package mvu.support

import java.util.ArrayList

class Dispatcher(private val parentDispatchers: List<(Action) -> Unit>, val dispatcher: (Action) -> Unit) {

    val allDispatchers: List<(Action) -> Unit>
        get() {
            val allDispatchers = ArrayList<(Action) -> Unit>()
            allDispatchers.add(dispatcher)
            allDispatchers.addAll(parentDispatchers)
            return allDispatchers
        }

    constructor(dispatcher: (Action) -> Unit) : this(ArrayList<(Action) -> Unit>(), dispatcher)

}

fun emptyDispatcher(): Dispatcher {
    return Dispatcher(ArrayList()) { }
}
