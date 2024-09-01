plugins {
	id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("jvm") version "1.9.22"
	application
}

group = "org.skriptlang.skript"
version = "2.9.1"

repositories {
    mavenCentral()
}

dependencies {
	implementation("net.minestom:minestom-snapshots:65f75bb059")
	implementation(project(":common"))
}

kotlin {
    jvmToolchain(21)
}

application {
	mainClass = "org.skriptlang.skript.minestom.MainKt"
}