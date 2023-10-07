import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.file.Paths

plugins {
    kotlin("jvm") version "1.9.10"
    id("gg.jte.gradle") version("3.1.3-SNAPSHOT")
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.junit.jupiter:junit-jupiter:5.4.2")
    implementation("gg.jte:jte-runtime:3.1.3-SNAPSHOT")
}

tasks.test {
    useJUnitPlatform()
}

tasks.generateJte {
    sourceDirectory = Paths.get(project.projectDir.absolutePath, "src", "main", "jte")
    contentType = gg.jte.ContentType.Html
}

sourceSets {
    main {
        java.srcDir(tasks.generateJte.get().targetDirectory)
    }
}

kotlin {
    jvmToolchain(8)
}