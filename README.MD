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

