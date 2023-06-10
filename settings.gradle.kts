pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net")
        maven("https://maven.architectury.dev/")
        maven("https://maven.minecraftforge.net")
        maven("https://repo.essential.gg/repository/maven-public")
        maven("https://repo.virtualclient.gg/artifactory/virtualclient-public/")
    }
    plugins {
        val egtVersion = "0.1.21-SNAPSHOT"
        id("gg.virtualclient.defaults.java") version egtVersion
        id("gg.virtualclient.defaults.loom") version egtVersion
        id("gg.virtualclient.defaults.repo") version egtVersion
        id("gg.virtualclient.multi-version.root") version egtVersion
        id("gg.virtualclient.multi-version.api-validation") version egtVersion
    }
}

rootProject.name = "Elementa"

include(":platform")
project(":platform").apply {
    projectDir = file("versions/")
    buildFileName = "root.gradle.kts"
}

listOf(
    "1.16.5-fabric",
    "1.17.1-fabric",
    "1.18.2-fabric",
    "1.19-fabric",
    "1.19.3-fabric",
    "1.19.4-fabric",
    "1.20.0-fabric",
).forEach { version ->
    include(":platform:$version")
    project(":platform:$version").apply {
        projectDir = file("versions/$version")
        buildFileName = "../build.gradle.kts"
    }
}

