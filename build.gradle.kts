// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath("com.buildkite.test-collector-android:unit-test-collector-plugin:0.1.0")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    id("com.buildkite.test-collector-android.unit-test-collector-plugin") version "0.1.0" apply false
}