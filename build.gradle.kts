plugins {
    `java-library`

}

repositories {
    jcenter()
}

dependencies {
    // This dependency is exported to consumers, that is to say found on their compile classpath.
    // api("org.apache.commons:commons-math3:3.6.1")

    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    // implementation("com.google.guava:guava:27.0.1-jre")
    implementation("com.vaadin:vaadin-server:8.7.1")

}
