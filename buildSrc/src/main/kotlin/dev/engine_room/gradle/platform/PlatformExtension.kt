package dev.engine_room.gradle.platform

import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.RenderDocRunTask
import net.fabricmc.loom.task.RunGameTask
import net.neoforged.moddevgradle.dsl.ModDevExtension
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.SourceSet
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import java.io.File

open class PlatformExtension(val project: Project) {
    fun setupLoomMod(vararg sourceSets: SourceSet) {
        project.the<LoomGradleExtensionAPI>().mods.maybeCreate("main").apply {
            sourceSets.forEach(::sourceSet)
        }
    }

    fun setupMdgMod(vararg sourceSets: SourceSet) {
        project.the<ModDevExtension>().mods.maybeCreate("main").apply {
            sourceSets.forEach(::sourceSet)
        }
    }

    fun setupLoomRuns() {
        val loom = project.the<LoomGradleExtensionAPI>();
        loom.runs.apply {
            named("client") {
                generateRunConfig = true

                // Turn on our own debug flags
                systemProperties.put("flw.dumpShaderSource", "true")
                systemProperties.put("flw.debugMemorySafety", "true")

                // Turn on mixin debug flags
                systemProperties.put("mixin.debug.export", "true")
                systemProperties.put("mixin.debug.verbose", "true")

                // 720p baby!
                programArguments.addAll("--width", "1280", "--height", "720")

                // Helpful when debugging issues
                programArguments.addAll("--renderDebugLabels", "--vulkanValidation")
            }

            // We're a client mod, but we need to make sure we correctly render when playing on a server.
            named("server") {
                generateRunConfig = true
            }
        }

        if (System.getProperty("os.name").equals("Linux")) {
            project.tasks.register("runClientTracy", RunGameTask::class.java, loom.runConfigs["client"]).configure {
                tracy {
                    tracyCapture.set(File("/usr/bin/tracy-capture"))
                    output.set(project.file("profile.tracy"))
                }
            }

            project.tasks.withType<RenderDocRunTask> {
                renderDocExecutable.set(File("/usr/bin/renderdoccmd"))
            }
        }
    }

    fun setupMdgRuns() {
        project.the<ModDevExtension>().runs.apply {
            create("client") {
                client()

                systemProperty("flw.dumpShaderSource", "true")
                systemProperty("flw.debugMemorySafety", "true")

                systemProperty("mixin.debug.export", "true")
                systemProperty("mixin.debug.verbose", "true")

                programArguments.add("--renderDebugLabels")

                programArguments.addAll("--width", "1280", "--height", "720")
            }

            // We're a client mod, but we need to make sure we correctly render when playing on a server.
            create("server") {
                server()

                programArgument("--nogui")
            }
        }
    }

    fun setupTestMod(sourceSet: SourceSet) {
        project.tasks.apply {
            val testModJar = register<Jar>("testModJar") {
                from(sourceSet.output)
                archiveClassifier = "testmod"
            }

            named<Task>("build").configure {
                dependsOn(testModJar)
            }
        }
    }
}
