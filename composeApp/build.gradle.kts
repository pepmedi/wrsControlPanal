import org.gradle.internal.impldep.jcifs.Config.load
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

version = "1.0.1"

val localProps = Properties().apply {
    file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}

val BASE_URL = localProps.getProperty("BASE_URL") ?: "https://fallback.com"

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting{
            kotlin.srcDirs("build/generated/kmpConfig")
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)

            implementation(libs.jetbrains.compose.navigation)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            api(libs.koin.core)

            implementation(libs.bundles.ktor)
            implementation(libs.bundles.coil)

            api(libs.datastore.preferences)
            api(libs.datastore)

            implementation(libs.sonner)
            implementation(compose.animation)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.okhttp)

            implementation("org.bouncycastle:bctls-jdk15on:1.70")
            implementation("org.conscrypt:conscrypt-openjdk-uber:2.5.2")

            implementation("org.apache.pdfbox:pdfbox:2.0.30")
        }
    }
}


compose.desktop {
    application {
        mainClass = "com.wrscpanel.in.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "WrsControlPanel"
            packageVersion = "1.0.0"

            windows {
                iconFile.set(project.file(".../wrs_logo.png"))
            }

            macOS {
                iconFile.set(project.file("src/desktopMain/resources/wrs_logo.icns"))
                bundleID = "com.wrscpanel.in"
                signing {
                    sign.set(true)
                    identity.set("Developer ID Application: PEPMEDIA (FBU9XLJ353)")
                }

                notarization {
                    appleID.set("pepmediapp@icloud.com")
                    password.set(System.getenv("APPLE_APP_SPECIFIC_PASSWORD"))
                    teamID.set("FBU9XLJ353")
                }
            }
        }
    }
}

// Generate Config.kt with the BASE_URL constant
tasks.register("generateKmpConfig") {
    val outputDir = file("build/generated/kmpConfig")
    inputs.property("BASE_URL", BASE_URL)
    outputs.dir(outputDir)

    doLast {
        val file = outputDir.resolve("Config.kt")
        file.writeText("""
            package com.wrscpanel.`in`.config

            object Config {
                const val BASE_URL = "$BASE_URL"
            }
        """.trimIndent())
    }
}

tasks.named("compileKotlinDesktop") {
    dependsOn("generateKmpConfig")
}


// getting the version from gradle.properties
val generateBuildProperties by tasks.registering {
    val outputFile = file("src/desktopMain/resources/build.properties")
    outputs.file(outputFile)
    doLast {
        outputFile.writeText("version=${project.version}")
    }
}

tasks.named("desktopProcessResources") {
    dependsOn(generateBuildProperties)
}