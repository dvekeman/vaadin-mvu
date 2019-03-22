plugins {
    `war`
    id("org.gretty") version "2.2.0"
}

repositories {
    jcenter()
}

dependencies {
    providedCompile("javax.servlet:javax.servlet-api:3.1.0")
    // This dependency is exported to consumers, that is to say found on their compile classpath.
    // api("org.apache.commons:commons-math3:3.6.1")

    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    // implementation("com.google.guava:guava:27.0.1-jre")

    implementation("com.vaadin:vaadin-server:8.7.1")
    implementation("com.vaadin:vaadin-client-compiled:8.7.1")
    implementation("com.vaadin:vaadin-themes:8.7.1")
    implementation("com.vaadin:vaadin-push:8.7.1")
    
//    implementation("vaadin-mvu:vaadin-mvu:0.1.1")
    // implementation(fileTree(dir: 'libs', include: ['*.jar'])
    implementation(files("/src/vaadin/vaadin-mvu/build/libs/vaadin-mvu-0.1.1.jar"))
}
