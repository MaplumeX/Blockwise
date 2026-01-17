plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    // Project modules
    implementation(project(":core:common"))
    implementation(project(":core:domain"))

    // Coroutines
    implementation(libs.bundles.coroutines)
    api(libs.kotlinx.coroutines.test)

    // Kotlinx DateTime
    implementation(libs.kotlinx.datetime)

    // JUnit5
    api(libs.bundles.junit5)

    // MockK
    api(libs.mockk)

    // Turbine (Flow testing)
    api(libs.turbine)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
