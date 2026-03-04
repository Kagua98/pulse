import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.vanniktech.publish)
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
    }

    jvm("desktop")

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { target ->
        target.binaries.framework {
            baseName = "pulse-log"
            isStatic = true
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

android {
    namespace = "io.pulse.log"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    configure(KotlinMultiplatform(javadocJar = JavadocJar.Empty()))

    coordinates(
        groupId = "io.github.kagua98",
        artifactId = "pulse-log",
        version = "1.0.0-alpha03",
    )

    pom {
        name.set("Pulse Log")
        description.set("Lightweight logging API for Pulse — use in feature modules. Prints to Logcat and stores in memory.")
        url.set("https://github.com/Kagua98/pulse")
        inceptionYear.set("2025")

        licenses {
            license {
                name.set("Apache License 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("kagua98")
                name.set("Kagua98")
                url.set("https://github.com/Kagua98")
            }
        }

        scm {
            url.set("https://github.com/Kagua98/pulse")
            connection.set("scm:git:git://github.com/Kagua98/pulse.git")
            developerConnection.set("scm:git:ssh://git@github.com/Kagua98/pulse.git")
        }
    }
}
