import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.10"
    id("gg.jte.gradle") version("3.0.3")
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.junit.jupiter:junit-jupiter:5.4.2")
    implementation("gg.jte:jte-runtime:3.0.3")
}

jte {
    generate()
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}