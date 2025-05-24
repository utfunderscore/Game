plugins {
    `maven-publish`
    `java-library`
}

group = "org.readutf.arena"
version = "1.2.3"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
}

subprojects {

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    group = rootProject.group
    version = rootProject.version

    java {
        withJavadocJar()
        withSourcesJar()
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                groupId = "org.readutf.arena"
                version = rootProject.version.toString()
                artifactId = project.name

                from(components["java"])
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
