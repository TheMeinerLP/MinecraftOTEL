rootProject.name = "MinecraftOTEL"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.lucko.me/")
    }

    versionCatalogs {
        create("libs") {
            version("paper.yml", "0.6.0")
            version("paper.run", "3.0.2")
            version("shadowJar", "9.2.2")

            version("paper", "1.21.8-R0.1-SNAPSHOT")
            version("opentelemetry", "1.57.0")
            version("spark", "0.1-SNAPSHOT")
            version("velocity", "3.4.0-SNAPSHOT")
            version("cloudnet", "4.0.0-RC14")

            library("paper", "io.papermc.paper", "paper-api").versionRef("paper")
            library("opentelemetry.api", "io.opentelemetry", "opentelemetry-api").versionRef("opentelemetry")
            library("opentelemetry.sdk.spi", "io.opentelemetry", "opentelemetry-sdk-extension-autoconfigure-spi").versionRef("opentelemetry")
            library("spark.api", "me.lucko", "spark-api").versionRef("spark")
            library("velocity.api", "com.velocitypowered", "velocity-api").versionRef("velocity")

            library("cloudnet-bom", "eu.cloudnetservice.cloudnet", "bom").versionRef("cloudnet")
            library("cloudnet-bridge", "eu.cloudnetservice.cloudnet", "bridge-api").withoutVersion()
            library("cloudnet-bridge-impl", "eu.cloudnetservice.cloudnet", "bridge-impl").withoutVersion()
            library("cloudnet-driver-impl", "eu.cloudnetservice.cloudnet", "driver-impl").withoutVersion()
            library("cloudnet-platform-inject", "eu.cloudnetservice.cloudnet", "platform-inject-api").withoutVersion()
            library("cloudnet-jvm-wrapper", "eu.cloudnetservice.cloudnet", "wrapper-jvm-api").withoutVersion()

            plugin("paper.yml", "net.minecrell.plugin-yml.paper").versionRef("paper.yml")
            plugin("paper.run", "xyz.jpenilla.run-paper").versionRef("paper.run")
            plugin("shadowJar", "com.gradleup.shadow").versionRef("shadowJar")

            bundle(
                "cloudnet",
                listOf(
                    "cloudnet-bridge",
                    "cloudnet-bridge-impl",
                    "cloudnet-driver-impl",
                    "cloudnet-platform-inject",
                    "cloudnet-jvm-wrapper"
                )
            )
        }
    }
}
