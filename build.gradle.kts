plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.spring") version "2.2.0"
    kotlin("plugin.jpa") version "2.2.0"
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "com.therohankumar"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.lavalink.dev/releases") }
    maven { url = uri("https://maven.lavalink.dev/snapshots") }
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    // JDA 6.4.1 — required for DAVE E2EE support (mandatory March 2026)
    // Do NOT exclude tink (transport) or opus-java (encoding)
    implementation("net.dv8tion:JDA:6.4.1")

    // DAVE E2EE — libdave-jvm 0.1.2 (maven.lavalink.dev/releases)
    implementation("moe.kyokobot.libdave:impl-jni:0.1.2")
    implementation("moe.kyokobot.libdave:adapter-jda:0.1.2")
    // Linux x86-64 for Docker prod; natives-darwin is a universal binary (ARM64 + x86-64)
    implementation("moe.kyokobot.libdave:natives-linux-x86-64:0.1.2")
    implementation("moe.kyokobot.libdave:natives-darwin:0.1.2")

    // LavaPlayer 2.2.6
    implementation("dev.arbjerg:lavaplayer:2.2.6")

    // youtube-source v2 (replaces deprecated built-in YT source, includes thumbnail support)
    implementation("dev.lavalink.youtube:v2:1.18.0")

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // PostgreSQL + Flyway
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    // .env file support for local development
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
