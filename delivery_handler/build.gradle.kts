import org.gradle.api.tasks.testing.logging.TestExceptionFormat.*
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.31"

    application
}

repositories { mavenCentral() }

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("org.apache.kafka:kafka-clients:3.1.0")
    implementation("org.apache.kafka:kafka-streams:3.1.0")
    implementation("org.apache.kafka:connect-runtime:3.1.0")
    implementation("io.confluent:kafka-json-serializer:5.0.1")
    implementation("com.google.code.gson:gson:2.2.4")
    implementation("io.arrow-kt:arrow-core:1.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.0.1")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

repositories {
    mavenCentral()
    maven(url = "https://packages.confluent.io/maven/")
}

application { mainClass.set("delivery_handler.AppKt") }

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest { attributes["Main-Class"] = "delivery_handler.AppKt" }

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)

    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions { jvmTarget = "1.8" }
}

tasks.withType<Test> {
    testLogging {
        events(FAILED, STANDARD_ERROR, SKIPPED)
        exceptionFormat = FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}
