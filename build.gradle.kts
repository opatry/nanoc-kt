
plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
}

val nanocKtVersion = libs.versions.nanocKt.get()

allprojects {
    group = "net.opatry"
    version = nanocKtVersion

    repositories {
        mavenCentral()
        google()
    }
}