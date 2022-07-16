import gg.essential.gradle.multiversion.StripReferencesTransform.Companion.registerStripReferencesAttribute
import gg.essential.gradle.util.*
import gg.essential.gradle.util.RelocationTransform.Companion.registerRelocationAttribute

plugins {
    kotlin("jvm") version "1.6.10"
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.8.0"
    id("org.jetbrains.dokka") version "1.6.10" apply false
    id("gg.essential.defaults")
}

kotlin.jvmToolchain {
    (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(8))
}
tasks.compileKotlin.setJvmDefault("all-compatibility")

val internal by configurations.creating {
    val relocated = registerRelocationAttribute("internal-relocated") {
        relocate("org.dom4j", "gg.essential.elementa.impl.dom4j")
        relocate("org.commonmark", "gg.essential.elementa.impl.commonmark")
        remapStringsIn("org.dom4j.DocumentFactory")
        remapStringsIn("org.commonmark.internal.util.Html5Entities")
    }
    attributes { attribute(relocated, true) }
}

val common = registerStripReferencesAttribute("common") {
    excludes.add("net.minecraft")
}

repositories {
    maven {
        url = uri("https://repo.virtualclient.gg/artifactory/virtualclient-public/")
    }
}

dependencies {
    compileOnly(libs.kotlin.stdlib.jdk8)
    compileOnly(libs.kotlin.reflect)
    compileOnly(libs.jetbrains.annotations)

    internal(libs.commonmark)
    internal(libs.commonmark.ext.gfm.strikethrough)
    internal(libs.commonmark.ext.ins)
    internal(libs.dom4j)
    implementation(prebundle(internal))

    // Depending on LWJGL3 instead of 2 so we can choose opengl bindings only
    compileOnly("org.lwjgl:lwjgl-opengl:3.3.1")

    compileOnly("gg.virtualclient:virtualminecraft:1.0.0-11604-SNAPSHOT") {
        attributes { attribute(common, true) }
    }
    api("net.kyori:adventure-api:4.10.1")

    compileOnly("com.google.code.gson:gson:2.2.4")
}

apiValidation {
    ignoredProjects.add("platform")
    ignoredPackages.add("com.example")
    nonPublicMarkers.add("org.jetbrains.annotations.ApiStatus\$Internal")
}
