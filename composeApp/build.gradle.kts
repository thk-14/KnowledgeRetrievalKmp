import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.*

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
}

sqldelight {
    databases {
        create("KbDatabase") {
            packageName.set("com.thk.knowledgeretrievalkmp.db")
            generateAsync.set(true)
        }
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm()

    js {
        browser() {
            compilations.named("main") {
                compilerOptions.configure {
                    sourceMap.set(false)
                }
            }
        }
        binaries.executable()
    }

//    @OptIn(ExperimentalWasmDsl::class)
//    wasmJs {
//        browser()
//        binaries.executable()
//    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            implementation(libs.timber)
            implementation(libs.androidx.browser)

            // ktor
            implementation(libs.ktor.client.okhttp)
            implementation(libs.ktor.client.android)
            implementation(libs.slf4j.simple)

            // Data Store
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.multiplatform.settings.datastore)

            // SqlDelight
            implementation(libs.sqldelight.android.driver)

            // Google
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.services.auth)
            implementation(libs.googleid)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.material3.windowsizeclass)
            implementation(libs.adaptive)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // navigation
            implementation(libs.navigation.compose)

            // ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.auth)

            // FileKit
            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs.compose)

            // Multiplatform-settings
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.coroutines)

            // crypto
            implementation(libs.krypto)

            // SqlDelight
            implementation(libs.sqldelight.coroutines.extensions)

            // Auth
            implementation(libs.kmpauth.google)
            implementation(libs.kmpauth.uihelper)

            // Datetime
            implementation(libs.kotlinx.datetime)

            // json
            implementation(libs.kotlinx.serialization.json)

            // precompose
            implementation(libs.precompose)
            implementation(libs.precompose.viewmodel)

            // Coil
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)

            // Markdown
            implementation(libs.multiplatform.markdown.renderer)
            implementation(libs.multiplatform.markdown.renderer.m3)
            implementation(libs.multiplatform.markdown.renderer.coil3)

            // Compottie
            implementation(libs.compottie)
            implementation(libs.compottie.resources)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)

            // ktor
            implementation(libs.ktor.client.cio)
            implementation(libs.logback.classic)
            implementation(libs.ktor.server.netty)
            implementation(libs.ktor.server.core)

            // Data Store
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.multiplatform.settings.datastore)

            // SqlDelight
            implementation(libs.sqldelight.jvm.driver)
        }
        webMain.dependencies {
            // ktor
            implementation(libs.ktor.client.js)

            // SqlDelight
            implementation(libs.sqldelight.web.worker.driver)
            implementation(npm("@cashapp/sqldelight-sqljs-worker", "2.1.0"))
            implementation(devNpm("copy-webpack-plugin", "13.0.1"))
            implementation(npm("sql.js", "1.13.0"))
        }
    }

    compilerOptions {
        optIn.add("com.russhwolf.settings.ExperimentalSettingsApi")
        optIn.add("com.russhwolf.settings.ExperimentalSettingsImplementation")
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("androidx.compose.animation.ExperimentalSharedTransitionApi")
        optIn.add("androidx.compose.material3.ExperimentalMaterial3Api")
        optIn.add("kotlinx.serialization.ExperimentalSerializationApi")
    }
}

android {
    namespace = "com.thk.knowledgeretrievalkmp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.thk.knowledgeretrievalkmp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        create("custom_debug") {
            val keystorePropertiesFile = rootProject.file("keystore/thk.debug.properties")
            val keystoreProperties = Properties()
            keystoreProperties.load(FileInputStream(keystorePropertiesFile))

            storeFile = file(keystoreProperties.getProperty("storeFile"))
            storePassword = keystoreProperties.getProperty("storePassword")
            keyAlias = keystoreProperties.getProperty("keyAlias")
            keyPassword = keystoreProperties.getProperty("keyPassword")
        }
        create("release") {
            val keystorePropertiesFile = rootProject.file("keystore/thk.release.properties")
            val keystoreProperties = Properties()
            keystoreProperties.load(FileInputStream(keystorePropertiesFile))

            storeFile = file(keystoreProperties.getProperty("storeFile"))
            storePassword = keystoreProperties.getProperty("storePassword")
            keyAlias = keystoreProperties.getProperty("keyAlias")
            keyPassword = keystoreProperties.getProperty("keyPassword")
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            signingConfig = signingConfigs.getByName("custom_debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "com.thk.knowledgeretrievalkmp.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "KMS"
            packageVersion = "1.0.0"
            vendor = "thk"

            modules("jdk.unsupported")
            modules.add("java.sql")

            windows {
                iconFile.set(project.file("icons/book_icon.ico"))
                console = true
                shortcut = true
            }
        }
    }
}
