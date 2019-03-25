import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    `maven-publish`
    signing
    kotlin("jvm") version "1.3.21"
}

repositories {
    jcenter()
}

group = "vaadin-mvu"
version = "0.3.1"

dependencies {
    implementation("com.vaadin:vaadin-server:8.7.1")
    implementation("javax.servlet:javax.servlet-api:3.1.0")
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.1")

    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.9")
    testImplementation("org.spekframework.spek2:spek-dsl-jvm:2.0.1")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:2.0.1")
    testRuntimeOnly("org.jetbrains.kotlin:kotlin-reflect:1.3.21")

}

// >> Unit Testing
tasks {
    test {
        useJUnitPlatform {
            includeEngines("spek2")
        }
    }
}
// << Unit Testing

// >> Kotlin Compilation
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
// << Kotlin Compilation

// Maven Publishing >>
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
            url = uri("file://$buildDir/repo")
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}
// << Maven Publishing