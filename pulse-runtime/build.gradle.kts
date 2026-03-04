import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.library)
    alias(libs.plugins.vanniktech.publish)
}

group = "io.github.kagua98"
version = "1.0.0-alpha04"

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
            baseName = "pulse"
            isStatic = true
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":pulse-log"))
            api(libs.ktor.client.core)
            implementation(libs.kotlinx.coroutines.core)

            implementation(compose.material3)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.runtime)
            implementation(compose.animation)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        androidMain.dependencies {
            compileOnly("com.squareup.leakcanary:leakcanary-android:2.14")
            compileOnly("com.squareup.okhttp3:okhttp:4.12.0")

            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.activity.compose)
        }
    }
}

android {
    namespace = "io.pulse"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// ──────────────────────────────────────────────────────────────────
//  Maven Central Publishing (via Vanniktech plugin)
// ──────────────────────────────────────────────────────────────────

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    configure(KotlinMultiplatform(javadocJar = JavadocJar.Empty()))

    coordinates(
        groupId = "io.github.kagua98",
        artifactId = "pulse",
        version = "1.0.0-alpha04",
    )

    pom {
        name.set("Pulse")
        description.set(
            "Cross-platform developer tools for Kotlin Multiplatform — " +
                "network inspector, log viewer, crash reporter, and more."
        )
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
