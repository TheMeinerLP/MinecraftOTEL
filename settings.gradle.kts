rootProject.name = "MinecraftOTEL"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    versionCatalogs {
        create("libs") {
            version("modrinth", "2.+")
            version("hangar", "0.1.3")
            version("paper.yml", "0.6.0")
            version("paper.run", "3.0.2")
            version("shadowJar", "9.2.2")

            version("paper", "1.21.8-R0.1-SNAPSHOT")

            library("paper", "io.papermc.paper", "paper-api").versionRef("paper")

            plugin("modrinth", "com.modrinth.minotaur").versionRef("modrinth")
            plugin("hangar", "io.papermc.hangar-publish-plugin").versionRef("hangar")
            plugin("paper.yml", "net.minecrell.plugin-yml.paper").versionRef("paper.yml")
            plugin("paper.run", "xyz.jpenilla.run-paper").versionRef("paper.run")
            plugin("shadowJar", "com.gradleup.shadow").versionRef("shadowJar")
        }
    }
}
