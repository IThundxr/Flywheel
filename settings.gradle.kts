pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.neoforged.net/releases/") {
            name = "NeoForged"
        }
        maven("https://maven.fabricmc.net") {
            name = "FabricMC"
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("1.0.0")
}

rootProject.name = "Flywheel"

include("common")
// TODO - Re-enable when FRAPI is real
//include("fabric")
//include("vanillinFabric")
// TODO - Re-enable neoforge when they finally decide to port to 26.2
//include("neoforge")
//include("vanillinNeoForge")
