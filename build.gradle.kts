import xyz.jpenilla.runpaper.task.RunServer

plugins {
    id("java")
    alias(libs.plugins.shadowJar)
    alias(libs.plugins.paper.run)
    alias(libs.plugins.paper.yml)
}

if (!File("$rootDir/.git").exists()) {
    logger.lifecycle(
        """
    **************************************************************************************
    You need to fork and clone this repository! Don't download a .zip file.
    If you need assistance, consult the GitHub docs: https://docs.github.com/get-started/quickstart/fork-a-repo
    **************************************************************************************
    """.trimIndent()
    ).also { System.exit(1) }
}
val supportedMinecraftVersions = listOf(
    "1.20.6",
    "1.21",
    "1.21.1",
    "1.21.2",
    "1.21.3",
    "1.21.4",
    "1.21.5",
    "1.21.6",
    "1.21.7",
    "1.21.8",
    "1.21.9",
    "1.21.10"
)

group = "dev.themeinerlp"
version = "1.4.0"

dependencies {
    compileOnly(libs.paper)
    compileOnly(libs.velocity.api)
    annotationProcessor(libs.velocity.api)
    implementation(libs.opentelemetry.api)
    implementation(libs.opentelemetry.sdk.spi)
    implementation(libs.gson)
    compileOnly(libs.spark.api)
}

tasks {
    named("build") {
        dependsOn(shadowJar)
    }

    supportedMinecraftVersions.forEach { serverVersion ->
        register<RunServer>("run-$serverVersion") {
            minecraftVersion(serverVersion)
            jvmArgs("-DPaper.IgnoreJavaVersion=true", "-Dcom.mojang.eula.agree=true", "-javaagent:../opentelemetry-javaagent.jar")
            group = "run paper"
            runDirectory.set(file("run-$serverVersion"))
            pluginJars(rootProject.tasks.shadowJar.map { it.archiveFile }.get())
        }
    }

    shadowJar {
        archiveClassifier.set("")
        relocate("org.bstats", "net.onelitefeather.antiredstoneclockremastered.org.bstats")
        relocate("com.google.gson", "dev.themeinerlp.minecraftotel.libs.gson")
        dependsOn(jar)
    }

    processResources {
        filesMatching("velocity-plugin.json") {
            expand("version" to project.version)
        }
    }
}

paper {
    main = "dev.themeinerlp.minecraftotel.paper.MinecraftOTELPaperPlugin"
    apiVersion = "1.19"
    authors = listOf("TheMeinerLP")
    foliaSupported = true
    hasOpenClassloader = true
    serverDependencies {
        register("spark") {
            this.required = false
        }
    }
}
