plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

dependencies {
    implementation(libs.commonmark)
    implementation(libs.mustache.compiler)

    implementation(project(":nanoc-kt-core"))
    implementation(project(":nanoc-kt-util"))
}