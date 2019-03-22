# Example 3: Calculator

Here is a screenshot to set the scene

![Vaadin MVU Calculator Example](https://github.com/dvekeman/vaadin-mvu/blob/master/examples/example3-calculator/doc/img/vaadin-mvu-example3.png)

To get it up and running quickly

```
./gradlew appRun
```

This will do a gradle build and start a Jetty server with the Vaadin application running.

```
21:56:51 INFO  Jetty 9.2.24.v20180105 started and listening on port 8080
21:56:51 INFO  vaadin-mvu-example runs at:
21:56:51 INFO    http://localhost:8080/example3-calculator
```

Navigate to `http://localhost:8080/example3-calculator`

The application has a root component (`mvu.sample.Main`) which holds the main model (the current state of the counter).
Its layout has .. components

- ...

This gives you an idea how an MVP implementation (see [Vaadin _Advanced Application Architectures_][1]) would compare to an MVU implementation.

## Notes

This example uses version 0.1.0

## TODO

[] More documentation on the example
[] Upgrade to 0.2.0 (or later)

[1]: https://vaadin.com/docs/v8/framework/advanced/advanced-architecture.html

