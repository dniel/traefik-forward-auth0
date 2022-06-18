/**
 * Gradle build file.
 * Building the microservice with the Kotlin plugin for gradle.
 *
 * @see <a href="https://kotlinlang.org/docs/reference/using-gradle.html">Using Gradle in Official Kotlin doc.</a
 */
plugins {
    kotlin("jvm") version "1.7.0"
    kotlin("plugin.serialization") version "1.7.0"

    kotlin("kapt")
    id("io.micronaut.application") version "3.2.0"
    id("org.jetbrains.kotlin.plugin.allopen")
    id("groovy")
    id("org.graalvm.buildtools.native") version "0.9.11"
//    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
}

group = "dniel.forwardauth"

val version: String by project
val micronautVersion: String by project
val spekVersion: String by project
val artifactGroup = group
val artifactVersion = version
val targetJvmVersion: String by project

repositories {
    mavenCentral()
}

micronaut {
    version(micronautVersion)
    runtime("netty")
    testRuntime("spock")
    processing {
        incremental(true)
        annotations("dniel.forwardauth.*")
    }
}


kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(17))
        this.vendor.set(JvmVendorSpec.GRAAL_VM)
    }
}

configurations.all {
    resolutionStrategy.dependencySubstitution {
        substitute(module("io.micronaut:micronaut-jackson-databind"))
                .using(module("io.micronaut.serde:micronaut-serde-jackson:1.1.0"))
    }
}

dependencies {
    annotationProcessor("io.micronaut.serde:micronaut-serde-processor:1.1.0")

    /**
     * Kotlin dependencies.
     */
    implementation(kotlin("reflect"))

    /**
     * Micronaut framework dependencies.
     *
     * micronaut-inject-java and micronaut-validation are omitted
     * due to the micronaut application plugin adding them by default.
     */
    kapt(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    kapt("io.micronaut:micronaut-http-validation")
    kapt("io.micronaut.openapi:micronaut-openapi")
    kapt("io.micronaut.security:micronaut-security")
    kapt("io.micronaut.micrometer:micronaut-micrometer-annotation")

    implementation(enforcedPlatform("io.micronaut:micronaut-bom:$micronautVersion"))
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")

    implementation("io.micronaut:micronaut-inject")
    implementation("io.micronaut:micronaut-validation")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-management")
    implementation("io.micronaut.discovery:micronaut-discovery-client")
    implementation("io.micronaut.micrometer:micronaut-micrometer-core")
    implementation("io.micronaut.micrometer:micronaut-micrometer-registry-prometheus")
    implementation("io.micronaut.reactor:micronaut-reactor")
    implementation("io.micronaut.reactor:micronaut-reactor-http-client")

    implementation("io.micronaut.security:micronaut-security")
    implementation("io.micronaut.security:micronaut-security-jwt")
    implementation("io.micronaut.problem:micronaut-problem-json")

    /**
     * Third-party dependencies.
     */
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.21")
    implementation("com.auth0:java-jwt:3.18.2")
    implementation("com.auth0:jwks-rsa:0.20.0")
    implementation("com.github.stateless4j:stateless4j:2.6.0")
    implementation("com.mashape.unirest:unirest-java:1.4.9")
    implementation("com.google.guava:guava:31.0.1-jre")

    runtimeOnly("ch.qos.logback:logback-classic:1.2.11")
    runtimeOnly("org.slf4j:jcl-over-slf4j:1.7.32")
    runtimeOnly("org.slf4j:jul-to-slf4j:1.7.32")
    runtimeOnly("org.slf4j:log4j-over-slf4j:1.7.32")
    implementation("io.swagger.core.v3:swagger-annotations")

    /**
     * TODO To much json serialization stuff, clean up and remove.
     */
    implementation("io.micronaut.serde:micronaut-serde-jackson:1.1.0")
    compileOnly("com.fasterxml.jackson.core:jackson-databind")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

    /**
     * Test dependency configurations.
     */
    testImplementation("io.mockk:mockk:1.12.1")

    testCompileOnly(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    testImplementation("org.spockframework:spock-core") {
        exclude("org.codehaus.groovy:groovy-all")
    }
    testImplementation("io.micronaut:micronaut-inject-groovy")
    testImplementation("io.micronaut.test:micronaut-test-spock")
    testImplementation("org.assertj:assertj-core")
}

application {
    mainClass.set("dniel.forwardauth.infrastructure.micronaut.Application")
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    // Optionally configure plugin
//    ktlint {
//        debug.set(true)
//    }
}

tasks {
    graalvmNative {
        binaries {
            named("main") {
                verbose.set(true)
                buildArgs.add("-H:+ReportUnsupportedElementsAtRuntime")
                buildArgs.add("-H:ClassInitialization=org.slf4j:build_time")
            }
        }
        metadataRepository {
            enabled.set(true)
        }
    }

    // use Google Distroless mostly-static image when generating the
    // native-image build Dockerfile.
    dockerfileNative {
        baseImage("gcr.io/distroless/cc-debian11:nonroot")
    }

    test {
        systemProperty("micronaut.environments", "test")
        systemProperty("micronaut.env.deduction", false)
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = targetJvmVersion
            javaParameters = true
        }
    }

    compileTestKotlin {
        kotlinOptions {
            jvmTarget = targetJvmVersion
            javaParameters = true
        }
    }

    (run) {
        doFirst {
            jvmArgs = listOf("-XX:TieredStopAtLevel=1", "-Dcom.sun.management.jmxremote")
        }
    }
}