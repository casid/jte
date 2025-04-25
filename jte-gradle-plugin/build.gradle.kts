plugins {
    id("java-gradle-plugin")
    id("maven-publish")
    id("com.gradle.plugin-publish") version "1.1.0"
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("gg.jte:jte:3.2.2-SNAPSHOT")
    implementation("gg.jte:jte-kotlin:3.2.2-SNAPSHOT") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-compiler-embeddable")
    }
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.1.10")
}

group = "gg.jte"
version = "3.2.2-SNAPSHOT"

tasks.withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

gradlePlugin {
    website = "https://jte.gg"
    vcsUrl = "https://github.com/casid/jte.git"

    plugins {
        create("simplePlugin") {
            id = "gg.jte.gradle"
            displayName = "jte gradle plugin"
            description = "Precompile all jte/kte templates to Java/Kotlin classes during gradle build"
            tags.set(listOf("jte", "kte", "templates", "compiler", "Java", "Kotlin"))
            implementationClass = "gg.jte.gradle.JteGradle"
        }
    }
}