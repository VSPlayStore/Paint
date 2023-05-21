import java.net.URI

// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath ("com.android.tools.build:gradle:8.0.1")
        classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.20")
        classpath ("com.google.gms:google-services:4.3.15")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven {
            url = URI("https://jitpack.io")
        }
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}
