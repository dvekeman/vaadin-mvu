package mvu.sample

import com.vaadin.data.Binder
import com.vaadin.server.Sizeable
import com.vaadin.ui.Alignment
import com.vaadin.ui.Component
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.VerticalLayout
import mvu.support.Action
import mvu.support.BroadcastAction
import mvu.support.Dispatcher
import mvu.support.UpdateKt
import mvu.support.ViewKt
import mvu.support.bindModelAndViewKt
import mvu.support.extra.BoundLabel
import mvu.support.extra.DispatchButton

internal object Main {

    data class MainModel(val ticker: Int)

    private val initialModel: MainModel = MainModel(0)


    // VIEW
    // This initial bindModelAndView
    fun view(): Component {
        return bindModelAndViewKt(initialModel, view, update)
    }

    private val view: ViewKt<MainModel> = { binder: Binder<MainModel>, dispatcher: Dispatcher ->
        val layout = HorizontalLayout()
        layout.defaultComponentAlignment = Alignment.MIDDLE_CENTER

        // the ticker label itself
        val tickerLabel = BoundLabel.builder<MainModel, Int>(binder, Int::class.java)
                .withValueProcessor { it.toString() }
                .withValueProvider { model -> model.ticker }
                .withEmptyValue(0)
                .forLabel { label -> label.setWidth(50f, Sizeable.Unit.PIXELS) }
                .build()

        val plus = DispatchButton.builder(dispatcher)
                .withCaption("+")
                .withAction { PlusAction() }
                .forButton { button -> button.addStyleName("btn-mono") }
                .forButton { button -> button.setWidth(50f, Sizeable.Unit.PIXELS) }
                .build()

        val minus = DispatchButton.builder(dispatcher)
                .withCaption("-")
                .withAction { MinusAction() }
                .forButton { button -> button.addStyleName("btn-mono") }
                .forButton { button -> button.setWidth(50f, Sizeable.Unit.PIXELS) }
                .build()

        // the button ( + <x> ) (+) (-) and ( - <x> )
        val buttonLayout = VerticalLayout()
        buttonLayout.addComponent(PlusX.view(dispatcher))
        buttonLayout.addComponent(plus)
        buttonLayout.setComponentAlignment(plus, Alignment.MIDDLE_RIGHT)
        buttonLayout.addComponent(minus)
        buttonLayout.setComponentAlignment(minus, Alignment.MIDDLE_RIGHT)
        buttonLayout.addComponent(MinusX.view(dispatcher))

        layout.addComponent(tickerLabel)
        layout.addComponent(buttonLayout)

        layout

    }

    class PlusAction : Action

    class MinusAction : Action

    data class PlusXAction(val increment: Int) : BroadcastAction

    data class MinusXAction(val decrement: Int) : BroadcastAction

    private val update: UpdateKt<MainModel> = { action: Action, oldModel: MainModel ->
        when (action) {
            is PlusAction ->
                oldModel.copy(ticker = oldModel.ticker + 1)
            is MinusAction ->
                oldModel.copy(ticker = oldModel.ticker - 1)
            is PlusXAction ->
                oldModel.copy(ticker =oldModel.ticker + action.increment)

            is MinusXAction ->
                oldModel.copy(ticker = oldModel.ticker - action.decrement)

            else -> oldModel
        }
    }
}
