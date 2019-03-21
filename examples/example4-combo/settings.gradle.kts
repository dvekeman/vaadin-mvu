rootProject.name = "example4-combo"

sourceControl {
    gitRepository(uri("https://github.com/dvekeman/vaadin-mvu.git")) {
        producesModule("vaadin-mvu:vaadin-mvu")
    }
}