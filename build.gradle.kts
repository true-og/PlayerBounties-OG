/* This is free and unencumbered software released into the public domain */

import org.gradle.kotlin.dsl.provideDelegate

/* ------------------------------ Plugins ------------------------------ */
plugins {
    id("java") // Import Java plugin.
    id("java-library") // Import Java Library plugin.
    id("com.diffplug.spotless") version "7.0.4" // Import Spotless plugin.
    id("com.gradleup.shadow") version "8.3.8" // Import Shadow plugin.
    eclipse // Import Eclipse plugin.
    kotlin("jvm") version "2.1.21" // Import Kotlin JVM plugin.
}

/* --------------------------- JDK / Kotlin ---------------------------- */
java {
    sourceCompatibility = JavaVersion.VERSION_17 // Compile with JDK 17 compatibility.
    toolchain { // Select Java toolchain.
        languageVersion.set(JavaLanguageVersion.of(17)) // Use JDK 17.
        vendor.set(JvmVendorSpec.GRAAL_VM) // Use GraalVM CE.
    }
}

kotlin { jvmToolchain(17) }

/* ----------------------------- Metadata ------------------------------ */
group = "com.tcoded"
version = "1.4.17"

val apiVersion = "1.19"

/* ----------------------------- Resources ----------------------------- */
tasks.named<ProcessResources>("processResources") {
    val props = mapOf("version" to version, "apiVersion" to apiVersion)
    inputs.properties(props)
    filesMatching("plugin.yml") { expand(props) }
    from("LICENSE") { into("/") }
}

/* ---------------------------- Repos ---------------------------------- */
repositories {
    mavenCentral()
    gradlePluginPortal()
    maven { url = uri("https://repo.purpurmc.org/snapshots") }
    maven { url = uri("file://${System.getProperty("user.home")}/.m2/repository") }
    System.getProperty("SELF_MAVEN_LOCAL_REPO")?.let {
        val dir = file(it)
        if (dir.isDirectory) {
            println("Using SELF_MAVEN_LOCAL_REPO at: $it")
            maven { url = uri("file://${dir.absolutePath}") }
        } else {
            logger.error("TrueOG Bootstrap not found, defaulting to ~/.m2 for mavenLocal()")
            mavenLocal()
        }
    } ?: logger.error("TrueOG Bootstrap not found, defaulting to ~/.m2 for mavenLocal()")
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://oss.sonatype.org/content/groups/public/") }
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://repo.codemc.org/repository/maven-public") }
    maven { url = uri("https://repo.glaremasters.me/repository/towny/") }
    maven { url = uri("https://repo.extendedclip.com/releases/") }
    maven { url = uri("https://repo.tcoded.com/public/") }
}

/* ---------------------- Java project deps ---------------------------- */
dependencies {
    compileOnly("org.spigotmc:spigot-api:1.19.3-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("org.jetbrains:annotations:23.0.0")
    compileOnly("com.github.CraptiCraft-Development:ClansLite-API:1.4.4")
    compileOnly("com.cortezromeo.clansplus:clansplus-plugin:2.8")
    implementation("com.github.TechnicallyCoded:FoliaLib:0.4.3")
    implementation("com.github.lightlibs:LegacyColorCodeParser:1.0.0")
    compileOnly("net.sacredlabyrinth.phaed.simpleclans:SimpleClans:2.15.2")
    compileOnly("com.github.SaberLLC:Saber-Factions:4.1.4-STABLE")
    compileOnly("com.palmergames.bukkit.towny:towny:0.100.0.0")
    compileOnly("me.clip:placeholderapi:2.11.6")
}

/* ---------------------- Reproducible jars ---------------------------- */
tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

/* ----------------------------- Shadow -------------------------------- */
tasks.shadowJar {
    archiveBaseName.set("PlayerBounties-OG")
    archiveClassifier.set("")
    minimize()
    relocate("com.tcoded.folialib", "net.trueog.playerbountiesog.lib.folialib")
    relocate("org.bstats", "net.trueog.playerbountiesog.lib.bstats")
}

tasks.jar {
    archiveBaseName.set("PlayerBounties-OG")
    archiveClassifier.set("part")
}

tasks.build { dependsOn(tasks.spotlessApply, tasks.shadowJar) }

/* --------------------------- Javac opts ------------------------------- */
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
    options.isFork = true
    options.compilerArgs.add("-Xlint:deprecation")
    options.encoding = "UTF-8"
}
