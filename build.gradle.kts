import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
    id("java")
    id("java-base")
    id("java-library")
    id("maven-publish")
    id("io.papermc.paperweight.userdev") version "1.7.2"
    id("xyz.jpenilla.run-paper") version "2.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}


group = "de.timesnake"
version = "2.0.0"
var projectId = 26

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }
    maven {
        url = uri("https://git.timesnake.de/api/v4/groups/7/-/packages/maven")
        name = "timesnake"
        credentials(PasswordCredentials::class)
    }
}

dependencies {
    compileOnly("de.timesnake:basic-bukkit:4.+")

    compileOnly("de.timesnake:database-bukkit:4.+")
    compileOnly("de.timesnake:database-api:4.+")

    compileOnly("de.timesnake:channel-bukkit:5.+")
    compileOnly("de.timesnake:channel-api:5.+")

    compileOnly("de.timesnake:library-packets:3.+")

    compileOnly("de.timesnake:library-commands:2.+")
    compileOnly("de.timesnake:library-permissions:2.+")
    compileOnly("de.timesnake:library-basic:2.+")
    compileOnly("de.timesnake:library-chat:2.+")

    paperweight.paperDevBundle("1.21-R0.1-SNAPSHOT")

    compileOnly("net.kyori:adventure-api:4.11.0")
}

configurations.configureEach {
    resolutionStrategy.dependencySubstitution {
        if (project.parent != null) {
            substitute(module("de.timesnake:basic-bukkit")).using(project(":cores:basic-bukkit"))

            substitute(module("de.timesnake:database-bukkit")).using(project(":database:database-bukkit"))
            substitute(module("de.timesnake:database-api")).using(project(":database:database-api"))

            substitute(module("de.timesnake:channel-bukkit")).using(project(":channel:channel-bukkit"))
            substitute(module("de.timesnake:channel-api")).using(project(":channel:channel-api"))

            substitute(module("de.timesnake:library-packets")).using(project(":libraries-mc:library-packets"))

            substitute(module("de.timesnake:library-commands")).using(project(":libraries:library-commands"))
            substitute(module("de.timesnake:library-permissions")).using(project(":libraries:library-permissions"))
            substitute(module("de.timesnake:library-basic")).using(project(":libraries:library-basic"))
            substitute(module("de.timesnake:library-chat")).using(project(":libraries:library-chat"))
        }
    }
}

tasks.register<Copy>("exportAsPlugin") {
    from(layout.buildDirectory.file("libs/${project.name}-${project.version}-all.jar"))
    into(findProperty("timesnakePluginDir") ?: "")

    dependsOn("shadowJar")
}

tasks.withType<PublishToMavenRepository> {
    dependsOn("shadowJar")
}

publishing {
    repositories {
        maven {
            url = uri("https://git.timesnake.de/api/v4/projects/$projectId/packages/maven")
            name = "timesnake"
            credentials(PasswordCredentials::class)
        }
    }

    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release = 21
    }

    processResources {
        inputs.property("version", project.version)

        filesMatching("plugin.yml") {
            expand(mapOf(Pair("version", project.version)))
        }
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
}

paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION