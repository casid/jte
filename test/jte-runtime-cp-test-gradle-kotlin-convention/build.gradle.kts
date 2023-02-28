import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.10"
    id("gg.jte.gradle") version("2.2.7-SNAPSHOT")
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.junit.jupiter:junit-jupiter:5.4.2")
    implementation("gg.jte:jte-runtime:2.2.7-SNAPSHOT")
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