// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false

    // extension to check and update dependencies or its version issues
    id("com.github.ben-manes.versions") version "0.51.0"
    alias(libs.plugins.kotlin.compose) apply false
}