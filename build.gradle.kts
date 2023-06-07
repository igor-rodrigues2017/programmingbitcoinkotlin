import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.0"
    application
}

group = "com.rodrigues"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.guava:guava:31.1-jre")
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")
    implementation("com.github.kittinunf.fuel:fuel:2.3.1")

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.8.20-RC")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:5.5.5")
    testImplementation("io.mockk:mockk:1.13.4")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.20-RC")

}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}