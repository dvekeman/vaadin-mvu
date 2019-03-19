# Vaadin MVU

This guide describes how to use the Model-View-Update pattern for Vaadin applications.

*Warning*: it is very opinionated and it's based on The Elm Architecture (TEA) which also influenced the Redux framework.

## Introduction

*Goals*

- No dependencies
- Lightweight
- Immutability (as much as possible)
- Composability

Having _no dependencies_ as a goal has big consequences. Frameworks like Rx could certainly play a role 
in implementing this pattern however I decided not to use it (or any equivalents) for several reasons.
Applying a pattern should not force you to use any dependencies. It would also increase the maintenance burdon
having to keep the pattern up-to-date with the latest version of the dependency or maybe even different version.

There is obviously one exception to the rule which is the dependency on Vaadin but really the only two actual dependencies there (so far at least) are

```
import com.vaadin.data.Binder;
import com.vaadin.ui.Component;
```

There is an optional `extra` package which includes some wrappers around existing components.

The pattern is rather _lightweight_ as it allows you to only apply it to individual components. You can easily integrate
it into another design pattern should you need to.

Achieving _immutability_ in Java is not that easy and comes with a certain cost. If the models would get super-huge
then this would become a problem. Currently it's marked 'experimental'.

Although I've tried to reduce some boilerplate (and will continue to do so), it is not a design goal at the moment.
*Composability* on the other hand is a goal and is one of the causes that there is more typing to be done.
Typically inheritance would be used to avoid some typing but I intentionally avoided that here as it would reduce 
the composability of the pattern and the components. This is often discussed under the famous title 'Composability over Inheritance'.

Inheritance is not the only way to reduce boiler plate. Alternative approaches are IDE code completion, Annotation processors, etc.
However at the moment it's just... the keyboard and you!

## Getting started

To get started I would actually recommend to go through the Elm getting started and then come back here.
You might possibly be disappointed about all the great things in Elm which are missing here but that's ok.

One major difference is that in Elm the update function has 

```
update : Msg -> Model -> ( Model, Cmd Msg)
```

whereas here the signature is

```
update : Action -> Model -> Model
```

This is mainly because this is still experimental and ideally I would like to match up the signatures.

Currently there is just one example but there should be more in the future.

The main example shows a ticker label together with four other components:

- (+) button to increment by one
- (-) button to decrement by one
- PlusX component to increment by a custom integer
- MinusX component to decrement by a custom integer

The `PlusX` and `MinusX` are themselves little Model-View-Update components they consist of 

- a TextField to enter the value
- a Button to apply the increment or decrement

A component consists of three sections which are typically implemented in the same class but nothing prevents you 
from spreading them out into their own classes.

- A Model which is a data class holding the state of the component
- A view which is a function rendering the model as a ReadOnly view on the state
- An update function (or dispatcher) which handles actions | events | messages which trigger an update to the model

Let's look at the _model_ for the PlusX component

```
static class Model {

    final int increment;
    final Model.Builder builder;

    private Model(Model.Builder builder) {
        this.builder = builder;
        this.increment = builder.increment;
    }

    static Model.Builder builder() {
        return new Model.Builder();
    }

    static Model copy(Model.Builder builder) {
        return new Model(builder);
    }

    static Model initialModel() {
        return builder().build();
    }

    static class Builder {
        int increment = 10;

        Model.Builder withIncrement(int increment) {
            this.increment = increment;
            return this;
        }

        Model build() {
            return new Model(this);
        }

    }

}
```

It's a static inner class because the model should only contain the state which is local to the component.

There is some boilerplate here though such as the `copy` and the `initialModel`. There are annotation processors
for builders (such as the AutoValue Builder from Google) which should reduce some of this boilerplate here.

Try to keep the model as immutable as possible. The fields are marked `final` for a good reason and there are not getters
and setters. A getter would of course be ok but given it's internal in this case we can just access the field directly without
breaking any encapsulation rules. Should you move the model to an upper class then it's probably best to introduce getters.

Updates to the model are done by copying the model (using the #copy(Model.Builder builder)) and using the `with...` methods on the builder.
For basic types this all works fine however for objects this might be more tricky: should we deep copy each object (I think ideally yes)?
Can it be achieved in a nice way?

All in all you can easily see that the model is just a simple data class holding some state, a increment value in this case.

Next is the _view_

```
static Component view(Consumer<Action> mainUpdater) {
    return ModelViewBinder.bindModelAndView(mainUpdater, Model.initialModel(), PlusX::view, PlusX::update);
}

private static Component view(Binder<PlusX.Model> binder, List<Consumer<Action>> dispatchers) {
    HorizontalLayout layout = new HorizontalLayout();

    TextField increment = BoundTextField.builder(binder)
            .withValueProvider(model -> Integer.toString(model.increment))
            .withValueConsumer(s -> new PlusX.SetIncrement(Integer.valueOf((String) s)))
            .withDispatchers(dispatchers)
            .forField(textField -> textField.addStyleName("SomeStyle"))
            .forField(textField -> textField.setWidth(50, Sizeable.Unit.PIXELS))
            .build();

    Button plus = DispatchButton.builder(dispatchers)
            .withCaption("+")
            .withAction(() -> new Main.PlusXAction(binder.getBean().increment))
            .forButton(button -> button.addStyleName("btn-mono"))
            .forButton(button -> button.setWidth(50, Sizeable.Unit.PIXELS))
            .build();

    layout.addComponent(increment);
    layout.addComponent(plus);

    return layout;
}

```

There are two methods but the first one should always be the same. It's simply binding the Model and the View together.

The second method renders a Vaadin component. In this case it's a horizontal layout with a textfield to enter the value to increment 
and a button to apply the action. Both subcomponents use the wrappers provided by the `extra` package but this is optional.

The `dispatchers` are a combination of the Main dispatcher and the PlusX dispatcher. The view function is unaware of which actions
are handled by which dispatcher however to spoil the secret here is what will happen:

- The PlusX dispatcher will intercept the SetIncrementAction from the increment textfield to store the value as local state.
- The PlusX dispatcher will ignore the Main.PlusXAction from the button.
- The Main dispatcher will ignore the SetIncrementAction (it even isn't aware of this action).
- The Main dispatcher will intercept the Main.PlusXAction from the button to apply the increment to the global ticker.

Internally the increment textfield is bound to the Model (to the increment field). There is a `withValueProvider` to convert the model increment field into a String.

```
increment
  .withValueProvider(model -> Integer.toString(model.increment))
```

When the user changes the value in the textfield an action is triggered:

```
increment
  .withValueConsumer(s -> new PlusX.SetIncrement(Integer.valueOf((String) s)))
```

This will be passed onto the dispatchers who can then appropriately react to it.

Similarly for the button:

```
plus
  .withAction(() -> new Main.PlusXAction(binder.getBean().increment))
```

This will inform all dispatchers in the chain (the Main and PlusX in this case) about the intent to increment by x.

Last but not least is the _update_ function:

Remember that the signature is

```
update : Action -> Model -> Model
```

So we consume an action and an existing model and then produce a new model:

```
private static Model update(Action action, Model oldModel) {
    if (action instanceof SetIncrement) {
        return Model.copy(oldModel.builder
                .withIncrement(((SetIncrement) action).increment)
        );
    } else {
        return oldModel;
    }
}
```

By going through the effort of copying the model I try to achieve the main goal of immutability which is to reduce
a class of bugs related to unwanted side-effects.

For someone new to this it probably looks cumbersome. For someone who has been through the effort of learning functional
programming I hope it looks ok, acceptable or even nice!

Personally I would like to have one extra feature in this function which is case analysis. In Elm, the compiler can know
if you forgot to handle an Msg in your update function. Here, due to the else case, these will go unnoticed.
On the other hand, in Elm, it's harder to trigger Actions on the Main (or parent) component whereas here that's easier (hopefully for the better).

As a final note, there is a `ComponentTemplate` in the `template` package which can be copy/pasted to create new components.

`() -> enjoy()`

## Future work

This is very incomplete and a few emerging questions are

- How to deal with bigger, more complex models
- How to deal with backend calls (REST, database, ...)
- Add support for more Vaadin components besides labels, textfields and buttons