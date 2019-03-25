package mvu.support.template

import com.vaadin.data.Binder
import com.vaadin.ui.Component
import com.vaadin.ui.HorizontalLayout
import mvu.support.Action
import mvu.support.Dispatcher
import mvu.support.UpdateKt
import mvu.support.ViewKt
import mvu.support.bindModelAndViewKt
import mvu.support.extra.BoundTextField
import mvu.support.extra.DispatchButton

/**
 * Basic Component template
 */
internal object ComponentTemplateKt {

    fun view(parentDispatcher: Dispatcher): Component {
        return bindModelAndViewKt(parentDispatcher, initialModel, view, update)
    }


    private data class Model(val value: Int)


    private val initialModel: Model = Model(0)


    private val view: ViewKt<Model> = { binder: Binder<Model>, dispatcher: Dispatcher ->
        val layout = HorizontalLayout()

        val textField = BoundTextField.builder(binder)
                // ...
                .build()
        layout.addComponent(textField)

        val button = DispatchButton.builder(dispatcher)
                // ...
                .build()
        layout.addComponent(button)

        layout
    }


    private data class SetValue(val value: Int) : Action


    private val update: UpdateKt<Model> = { action: Action, oldModel: Model ->
        if (action is SetValue) {
            oldModel.copy(value = action.value)
        } else {
            oldModel
        }
    }


}
