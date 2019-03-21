plugins {
    `java-library`
    `maven-publish`
    signing
}

repositories {
    jcenter()
}

group = "vaadin-mvu"
version = "0.1.1"

dependencies {
    // This dependency is exported to consumers, that is to say found on their compile classpath.
    // api("org.apache.commons:commons-math3:3.6.1")

    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    // implementation("com.google.guava:guava:27.0.1-jre")
    implementation("com.vaadin:vaadin-server:8.7.1")

}

tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allJava)
}

tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc.get().destinationDir)
}

publishing {
    publications {
        create<MavenPublication>("myLibrary") {
            from(components["java"])
        }

        create<MavenPublication>("mavenJava") {
            from(components["java"])

            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
        }
    }

    repositories {
        maven {
            name = "myRepo"
            url = uri("file://${buildDir}/repo")
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}