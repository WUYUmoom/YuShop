plugins {
    id("fabric-loom") version "1.10-SNAPSHOT"
    id("maven-publish")
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
}
group = "com.wuyumoom"
version = "1.0.0-SNAPSHOT"

loom {
    splitEnvironmentSourceSets()

}
repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://nexus.wuyumoom.top:2026/repository/maven-public/")
    }
    maven {
        url = uri("https://maven.impactdev.net/repository/development/")
    }

    maven {
        name = "spigotmc-repo"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }

    maven {
        name = "CodeMC"
        url = uri("https://repo.codemc.io/repository/maven-public/")
    }
    maven {
        name = "placeholderapi"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
}
dependencies {
    /*<-minecraft->*/
    minecraft("com.mojang:minecraft:1.21.1")
    mappings(loom.officialMojangMappings())
    //mappings "net.fabricmc:yarn:1.21.1+build.3:v2"

    compileOnly("wuyumoom:yucore:1.6.6:YuCore@jar")
    /*<-cobblemon->*/

    /*<-spigot->*/
    compileOnly("org.spigotmc:spigot-api:1.21.1-R0.1-SNAPSHOT")
    //PAPI
    compileOnly("me.clip:placeholderapi:2.11.6")
    //NBTAPI
    compileOnly("de.tr7zw:item-nbt-api-plugin:2.14.2-SNAPSHOT")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
}
tasks.named<ProcessResources>("processResources") {
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(
            "version" to (project.version as String).replace("-SNAPSHOT", ""),
            "description" to "经典相片V3全新版本",
            "author" to "WUYUmoom"
        )
    }
}
tasks.named<Test>("test") {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}