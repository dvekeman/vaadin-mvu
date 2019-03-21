# Model View Update for Vaadin - Examples

For an introduction the MVU pattern for Vaadin, see my [vaadin-mvu][https://github.com/dvekeman/vaadin-mvu] library.

## Example 1: Counter

Here is a screenshot to set the scene

![Vaadin MVU Ticker Example](https://raw.githubusercontent.com/dvekeman/vaadin-mvu-example/master/doc/img/vaadin-mvu-example1.png)

To get it up and running quickly

```
./gradlew appRun
```

This will do a gradle build and start a Jetty server with the Vaadin application running.

```
21:56:51 INFO  Jetty 9.2.24.v20180105 started and listening on port 8080
21:56:51 INFO  vaadin-mvu-example runs at:
21:56:51 INFO    http://localhost:8080/vaadin-mvu-example
```

Navigate to `http://localhost:8080/vaadin-mvu-example`

The application has a root component (`mvu.sample.Main`) which holds the main model (the current state of the counter).
Its layout has 5 components

- A label with the value of the counter
- A plus button (which increments by 1)
- A minus button (which decrements by 1)
- A PlusX component
- A MinusX component

This demostrates how to compose an application out of subcomponents: PlusX and MinusX are themselves MVU components holding their own state and containing their own actions.