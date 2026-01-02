@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    `maven-publish`
    signing
}

group = "com.github.h0tk3y.better-parse"
// Version is managed by buildSrc/settings.gradle.kts

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_23)
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }

    js {
        browser()
        nodejs()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }

    linuxX64()
    macosX64()
    macosArm64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    mingwX64()

    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        // Shared Native Scope
        val nativeMain by creating {
            dependsOn(commonMain)
        }

        // Link all native targets to 'nativeMain'
        targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
            compilations["main"].defaultSourceSet.dependsOn(nativeMain)
        }

        // JS
        val jsMain by getting {
            dependsOn(commonMain)
        }
        val jsTest by getting {
            dependsOn(commonTest)
        }

        // Wasm
        val wasmJsMain by getting {
            dependsOn(commonMain)
        }
        val wasmJsTest by getting {
            dependsOn(commonTest)
        }
    }
}


val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

publishing {
    publications.withType<MavenPublication> {
        artifact(javadocJar)

        pom {
            name.set("better-parse")
            description.set("A nice parser combinator library for Kotlin")
            url.set("https://github.com/h0tk3y/better-parse")

            licenses {
                license {
                    name.set("Apache-2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0")
                }
            }
            developers {
                developer {
                    id.set("h0tk3y")
                    name.set("Dmitry Kichinsky")
                }
            }
            scm {
                url.set("https://github.com/h0tk3y/better-parse")
                connection.set("scm:git:https://github.com/h0tk3y/better-parse.git")
            }
        }
    }
}