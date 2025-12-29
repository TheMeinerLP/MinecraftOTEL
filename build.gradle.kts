import io.papermc.hangarpublishplugin.model.Platforms
import xyz.jpenilla.runpaper.task.RunServer

plugins {
    id("java")
    alias(libs.plugins.shadowJar)
    alias(libs.plugins.paper.run)
    alias(libs.plugins.paper.yml)
    alias(libs.plugins.hangar)
    alias(libs.plugins.modrinth)
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
version = "1.0.0-SNAPSHOT"

dependencies {
    compileOnly(rootProject.libs.paper)
    compileOnly("io.opentelemetry:opentelemetry-api:1.45.0")
    compileOnly("me.lucko:spark-api:0.1-SNAPSHOT")
}

tasks {
    named("build") {
        dependsOn(shadowJar)
    }

    supportedMinecraftVersions.forEach { serverVersion ->
        register<RunServer>("run-$serverVersion") {
            minecraftVersion(serverVersion)
            jvmArgs("-DPaper.IgnoreJavaVersion=true", "-Dcom.mojang.eula.agree=true")
            group = "run paper"
            runDirectory.set(file("run-$serverVersion"))
            pluginJars(rootProject.tasks.shadowJar.map { it.archiveFile }.get())
        }
    }

    shadowJar {
        archiveClassifier.set("")
        relocate("org.bstats", "net.onelitefeather.antiredstoneclockremastered.org.bstats")
        dependsOn(jar)
    }
    this.modrinth {
        dependsOn(shadowJar)
    }

    this.publishAllPublicationsToHangar {
        dependsOn(shadowJar)
    }
}

paper {
    main = "dev.themeinerlp.minecraftotel.MinecraftOTELPlugin"
    apiVersion = "1.19"
    authors = listOf("TheMeinerLP")
    foliaSupported = true
}
val baseVersion = version as String
val baseChannel = with(baseVersion) {
    when {
        contains("SNAPSHOT", true) -> "Snapshot"
        contains("ALPHA", true) -> "Alpha"
        contains("BETA", true) -> "Beta"
        else -> "Release"
    }
}
val changelogContent = "See [GitHub](https://github.com/TheMeinerLP/MinecraftOTEL/releases/tag/$baseVersion) for release notes."
hangarPublish {
    publications.register("MinecraftOTEL") {
        version.set(baseVersion)
        channel.set(baseChannel)
        changelog.set(changelogContent)
        apiKey.set(System.getenv("HANGAR_SECRET"))
        id.set("MinecraftOTEL")

        platforms {
            register(Platforms.PAPER) {
                jar.set(tasks.shadowJar.flatMap { it.archiveFile })
                platformVersions.set(supportedMinecraftVersions)
            }
        }
    }
}
modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("UWh9tyEa")
    versionType.set(baseChannel.lowercase())
    versionNumber.set(baseVersion)
    versionName.set(baseVersion)
    changelog.set(changelogContent)
    changelog.set(changelogContent)
    uploadFile.set(tasks.shadowJar.flatMap { it.archiveFile })
    gameVersions.addAll(supportedMinecraftVersions)
    loaders.add("paper")
    loaders.add("bukkit")
}
