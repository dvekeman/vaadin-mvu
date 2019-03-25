package mvu.sample

import com.vaadin.data.Binder
import com.vaadin.server.Sizeable
import com.vaadin.ui.Component
import com.vaadin.ui.HorizontalLayout
import mvu.support.Action
import mvu.support.Dispatcher
import mvu.support.UpdateKt
import mvu.support.ViewKt
import mvu.support.bindModelAndViewKt
import mvu.support.extra.BoundTextField
import mvu.support.extra.DispatchButton

internal object MinusX {


    private data class Model (val decrement: Int)


    private val initialModel : Model = Model(10)


    fun view(mainUpdater: Dispatcher): Component {
        return bindModelAndViewKt(mainUpdater, initialModel, view, update)
    }


    private val view: ViewKt<Model> = { binder: Binder<Model>, dispatcher: Dispatcher ->
        val layout = HorizontalLayout()

        val increment = BoundTextField.builder(binder)
                .withValueProvider { model -> Integer.toString(model.decrement) }
                .withValueConsumer { s -> SetDecrement(Integer.valueOf(s as String)) }
                .withDispatcher(dispatcher)
                .forField { textField -> textField.addStyleName("SomeStyle") }
                .forField { textField -> textField.setWidth(50.0F, Sizeable.Unit.PIXELS) }
                .build()
        layout.addComponent(increment)

        val plus = DispatchButton.builder(dispatcher)
                .withCaption("-")
                .withAction { Main.MinusXAction(binder.bean.decrement) }
                .forButton { button -> button.addStyleName("btn-mono") }
                .forButton { button -> button.setWidth(50f, Sizeable.Unit.PIXELS) }
                .build()
        layout.addComponent(plus)

        layout
    }


    private data class SetDecrement (val decrement: Int) : Action


    private val update: UpdateKt<Model> = {action: Action, oldModel: Model ->
        if (action is SetDecrement) {
            oldModel.copy(decrement = action.decrement)
        } else {
            oldModel
        }
    }


}
