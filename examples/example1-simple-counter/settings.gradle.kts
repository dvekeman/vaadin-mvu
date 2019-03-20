rootProject.name = "example1-simple-counter"

sourceControl {
    gitRepository(uri("https://github.com/dvekeman/vaadin-mvu.git")) {
        producesModule("vaadin-mvu:vaadin-mvu")
    }
}