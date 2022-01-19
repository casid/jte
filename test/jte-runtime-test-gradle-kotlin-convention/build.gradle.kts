
plugins {
    id("java")
    id("gg.jte.gradle").version("2.0.0-SNAPSHOT")
}

repositories {
    mavenCentral()
    mavenLocal()
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation("gg.jte:jte-runtime:2.0.0-SNAPSHOT")
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
    testImplementation("org.assertj:assertj-core:3.20.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")
}

jte {
    precompile()
}

tasks.jar {
    dependsOn(tasks.precompileJte)
    from(fileTree("jte-classes") {
        include("**/*.class")
    })
}