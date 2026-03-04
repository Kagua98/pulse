// Root project — build logic lives in submodules:
//   :pulse-log     — lightweight logging API (feature modules)
//   :pulse-runtime — full library with UI (app module, debugImplementation)

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.vanniktech.publish) apply false
}
