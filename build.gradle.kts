plugins {
	`maven-publish`
}

group = "org.skriptlang"
version = "2.9.1"

repositories {
	mavenCentral()
}

subprojects {
	apply(plugin = "java")
	apply(plugin = "maven-publish")

	publishing {
		publications {
			create<MavenPublication>("maven") {
				groupId = "org.skriptlang.skript"
				artifactId = project.name
				version = rootProject.version.toString()

				from(components["java"])
			}
		}
	}
}