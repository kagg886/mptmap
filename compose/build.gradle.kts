import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)

    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
}

val SKIP_SIGN = (System.getenv("SKIP_SIGN") ?: project.findProperty("SKIP_SIGN") as? String ?: "false").toBooleanStrict()
val LIB_COMPOSE_VERSION = System.getenv("LIB_COMPOSE_VERSION") ?: project.findProperty("LIB_COMPOSE_VERSION") as? String ?: "unsetted."
check(LIB_COMPOSE_VERSION.startsWith("v")) {
    "LIB_COMPOSE_VERSION not supported, current is $LIB_COMPOSE_VERSION"
}

group = "top.kagg886.mptmap"
version = LIB_COMPOSE_VERSION.substring(1)

println("LIB_COMPOSE_VERSION: $version")

kotlin {
    jvmToolchain(17)

    androidTarget { publishLibraryVariants("release") }
    jvm()
    wasmJs { browser() }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(libs.kotlinx.coroutines.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)

            implementation(libs.kotlinx.coroutines.swing)
        }

    }

    //https://kotlinlang.org/docs/native-objc-interop.html#export-of-kdoc-comments-to-generated-objective-c-headers
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        compilations["main"].compileTaskProvider.configure {
            compilerOptions {
                freeCompilerArgs.add("-Xexport-kdoc")
            }
        }
    }
}

android {
    namespace = group.toString()
    compileSdk = 35

    defaultConfig {
        minSdk = 21
    }
}

mavenPublishing {
    configure(
        KotlinMultiplatform(
            // whether to publish a sources jar
            sourcesJar = true,
        )
    )
    publishToMavenCentral(SonatypeHost.S01)
    if (!SKIP_SIGN) {
        signAllPublications()
    }
    coordinates(group.toString(), project.name, version.toString())
    pom {
        name = "MPTMap"
        description = "An implementation of tile map for Compose Multiplatform."
        inceptionYear = "2025"
        url = "https://github.com/kagg886/mptmap"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "kagg886"
                name = "kagg886"
                url = "https://github.com/kagg886/"
            }
        }
        scm {
            url = "https://github.com/kagg886/mptmap"
            connection = "scm:git:git://github.com/kagg886/mptmap.git"
            developerConnection = "scm:git:ssh://git@github.com/kagg886/mptmap.git"
        }
    }
}
