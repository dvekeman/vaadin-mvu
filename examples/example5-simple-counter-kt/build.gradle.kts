plugins {
    war
    id("org.gretty") version "2.2.0"
    kotlin("jvm") version "1.3.21"
}

repositories {
    jcenter()
}

dependencies {
    providedCompile("javax.servlet:javax.servlet-api:3.1.0")

    implementation("com.vaadin:vaadin-server:8.7.1")
    implementation("com.vaadin:vaadin-client-compiled:8.7.1")
    implementation("com.vaadin:vaadin-themes:8.7.1")

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.21")

    implementation("vaadin-mvu:vaadin-mvu:0.3.0")

}
