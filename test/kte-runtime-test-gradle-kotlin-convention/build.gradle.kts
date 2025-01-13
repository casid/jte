
plugins {
    kotlin("jvm") version "1.9.10"
    id("gg.jte.gradle") version("3.1.17-SNAPSHOT")
}

repositories {
    mavenCentral()
    mavenLocal()
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.junit.jupiter:junit-jupiter:5.9.2")
    implementation("gg.jte:jte-runtime:3.1.17-SNAPSHOT")
}

jte {
    precompile()
    kotlinCompileArgs.set(arrayOf("-jvm-target", "17"))
}

tasks.jar {
    dependsOn(tasks.precompileJte)
    from(fileTree("jte-classes") {
        include("**/*.class")
    })
}