package mvu.support

import com.vaadin.data.Binder
import com.vaadin.server.VaadinSession
import com.vaadin.shared.communication.PushMode
import com.vaadin.ui.Button
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.PushConfiguration
import com.vaadin.ui.UI
import io.mockk.every
import io.mockk.mockk
import mvu.support.extra.DispatchButton
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

object ModelViewBinderSpec : Spek({
    group("default group") {

        val vaadinPushConfigurationMock = mockk<PushConfiguration>()
        every { vaadinPushConfigurationMock.pushMode } answers { PushMode.DISABLED }

        val vaadinUIMock = mockk<UI>()
        every { vaadinUIMock.pushConfiguration } answers { vaadinPushConfigurationMock }

        val vaadinSessionMock = mockk<VaadinSession>()
        every { vaadinSessionMock.uIs } answers { listOf(vaadinUIMock) }
        VaadinSession.setCurrent(vaadinSessionMock)

        describe("A dummy Model View Update") {

            // MODEL
            val testModel = 42

            // VIEW
            val component = HorizontalLayout()
            val componentView: ViewKt<Int> = { _: Binder<Int>, _: Dispatcher -> component }

            // UPDATE
            val identityUpdate = { _: Action, model: Int -> model }


            // fun <MODEL> bindModelAndViewKt(model: MODEL, view: ViewKt<MODEL>, update: UpdateKt<MODEL>): Component
            describe("A simple bound view (no parent dispatchers)") {
                val boundComponent = bindModelAndViewKt(testModel, view = componentView, update = identityUpdate)
                it("Should return my view component") {
                    assertEquals(expected = component, actual = boundComponent)
                }
            }

        }

        describe("A Counter Model View Update") {
            // MODEL
            data class Model(val value: Int = 0)

            val initialModel = Model()

            var plusOneButton = Button()

            class IncAction : Action
            class BroadcastIncAction : BroadcastAction

            // VIEW
            var binder: Binder<Model>? = null
            val regularView: ViewKt<Model> = { viewBinder: Binder<Model>, dispatcher: Dispatcher ->
                binder = viewBinder
                plusOneButton = DispatchButton.builder(dispatcher)
                        .withAction { IncAction() }
                        .withCaption("+1")
                        .build()
                plusOneButton
            }

            // UPDATE
            val update = { action: Action, model: Model ->
                when (action) {
                    is IncAction, is BroadcastIncAction ->
                        model.copy(value = model.value + 1)
                    else -> model
                }
            }

            describe("A root component (no parent)") {
                // VIEW
                describe("A click on the +1 button") {
                    it("Should trigger an update to the model") {
                        bindModelAndViewKt(initialModel, regularView, update)
                        plusOneButton.click()
                        assertNotNull(binder)
                        assertEquals(expected = 1, actual = binder?.bean?.value)
                    }
                }
            }

            describe("A sub component (with parent)") {
                // Simulate some state in a parent component >>
                var parentValue = 1
                val parentDispatcher = Dispatcher { action ->
                    when (action) {
                        is BroadcastIncAction -> parentValue += 41
                        else -> {
                        }
                    }
                }
                // <<

                // fun <MODEL> bindModelAndViewKt(parentDispatcher: Dispatcher, model: MODEL, view: ViewKt<MODEL>, update: UpdateKt<MODEL>): Component
                describe("A click on the +1 button with a normal action") {
                    it("Should trigger an update to the model, but not the parent model") {
                        bindModelAndViewKt(parentDispatcher, initialModel, regularView, update)
                        plusOneButton.click()
                        assertNotNull(binder)
                        assertEquals(expected = 1, actual = binder?.bean?.value)

                        // The main state did **not** change because the IncAction is a normal `Action`, not a `BroadcastAction`
                        assertEquals(expected = 1, actual = parentValue)
                    }
                }

                describe("A click on the +1 button with a broadcast action") {
                    val broadcastView: ViewKt<Model> = { viewBinder: Binder<Model>, dispatcher: Dispatcher ->
                        binder = viewBinder
                        plusOneButton = DispatchButton.builder(dispatcher)
                                .withAction { BroadcastIncAction() }
                                .withCaption("+1")
                                .build()
                        plusOneButton
                    }

                    it("Should trigger an update to the model AND the parent model") {
                        bindModelAndViewKt(parentDispatcher, initialModel, broadcastView, update)
                        plusOneButton.click()
                        assertNotNull(binder)
                        assertEquals(expected = 1, actual = binder?.bean?.value)

                        // The main state **did change** because the IncAction is a `BroadcastAction`
                        assertEquals(expected = 42, actual = parentValue)
                    }
                }
            }

        }

    }

})