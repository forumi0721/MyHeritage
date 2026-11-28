plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
}

// Private repositories may provide this file; public repositories work without it.
val updateToken = rootProject.file("update-token.txt").takeIf { it.isFile }?.readText()?.trim().orEmpty()
val escapedUpdateToken = updateToken.replace("\\", "\\\\").replace("\"", "\\\"")

val appVersion = providers.gradleProperty("appVersion").orElse("1.0.0").get()
val appVersionParts = appVersion.split(".").map(String::toInt)
require(appVersionParts.size == 3 && appVersionParts.all { it in 0..999 }) {
    "appVersion must use MAJOR.MINOR.PATCH with each part between 0 and 999"
}
val computedVersionCode = appVersionParts[0] * 1_000_000 + appVersionParts[1] * 1_000 + appVersionParts[2]


android {
    namespace = "kr.stonecold.myheritage"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }
    defaultConfig {
        applicationId = "kr.stonecold.myheritage"
        minSdk = 33
        targetSdk = 37
        versionCode = computedVersionCode
        versionName = appVersion
        buildConfigField("String", "GITHUB_TOKEN", "\"$escapedUpdateToken\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val releaseStore = providers.gradleProperty("keystoreFile")
                .map(::File)
                .orElse(File(projectDir, "release-keystore.jks"))
                .get()
            storeFile = releaseStore
            storePassword = System.getenv("KEY_STORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
      compose = true
      buildConfig = true
    }

}

dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  androidTestImplementation(composeBom)

  // Core Android dependencies
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)

  // Arch Components
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material.icons.extended)
  // Tooling
  debugImplementation(libs.androidx.compose.ui.tooling)
  // Instrumented tests
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // Local tests: jUnit, coroutines, Android runner
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)

  // Instrumented tests: jUnit rules and runners
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.espresso.core)

  // Navigation
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)

  // Room
  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.room.ktx)
  ksp(libs.androidx.room.compiler)

  // WorkManager (Background Notification Scheduler)
  implementation(libs.androidx.work.runtime.ktx)
}
