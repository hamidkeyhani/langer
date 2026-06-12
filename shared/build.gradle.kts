import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    applyDefaultHierarchyTemplate()
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    jvm()
    
    js {
        browser()
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    
    androidLibrary {
       namespace = "com.example.langer.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation("io.ktor:ktor-client-okhttp:3.0.1")
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(compose.materialIconsExtended)
            
            // Ktor Client core dependencies
            implementation("io.ktor:ktor-client-core:3.0.1")
            implementation("io.ktor:ktor-client-content-negotiation:3.0.1")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.1")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jsMain.dependencies {
            implementation(libs.wrappers.browser)
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:3.0.1")
            }
        }
        val iosMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:3.0.1")
            }
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}