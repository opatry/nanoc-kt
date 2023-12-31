plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

dependencies {
    testImplementation(libs.junit4)

    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.yamlbeans)

    implementation(libs.reflections)
}
