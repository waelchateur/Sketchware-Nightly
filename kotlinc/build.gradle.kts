plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.android")
    id("com.android.library")
    id("maven-publish")
}

android {
    namespace = "org.jetbrains.kotlin"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    // This dependency is exported to consumers, that is to say found on their compile classpath.
    implementation("org.lsposed.hiddenapibypass:hiddenapibypass:4.3")

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.22")
    implementation("io.github.itsaky:nb-javac-android:17.0.0.3")
    implementation("org.jetbrains.kotlin:kotlin-script-runtime:2.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4")
    implementation("org.jetbrains.intellij.deps:trove4j:1.0.20200330")
    implementation("org.jdom:jdom:2.0.2")

    implementation(projects.jaxp)
    api(files("libs/kotlin-compiler-2.1.10.jar"))
    api "org.jetbrains.kotlin:kotlin-stdlib:2.0.0"
    compileOnly(projects.theUnsafe)
}