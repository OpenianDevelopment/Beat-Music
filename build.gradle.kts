plugins {
    kotlin("jvm") version "2.0.21"
    application
}

group = "com.therohankumar"
version = "2.0"

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
    maven("https://maven.arbjerg.dev/releases")
    maven("https://maven.lavalink.dev/releases")
    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("net.dv8tion:JDA:5.2.2")
    implementation("dev.arbjerg:lavaplayer:2.2.3")
    implementation("dev.arbjerg:lavadsp:0.7.8")
    implementation("com.github.rohank05:lavadsp-extended:0.0.5")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
    implementation("ch.qos.logback:logback-classic:1.5.16")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("dev.lavalink.youtube:v2:1.11.5")
    implementation("dev.arbjerg:lavaplayer-ext-youtube-rotator:2.2.2")
    testImplementation(kotlin("test"))

}

// Specify the main class
application {
    mainClass.set("com.therohankumar.MainKt") // Replace with your actual main class
}

tasks {
    test {
        useJUnitPlatform()
    }
    jar {
        manifest {
            attributes["Main-Class"] = application.mainClass.get()
        }

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        from(sourceSets.main.get().output)
        dependsOn(configurations.runtimeClasspath)
        from({
            configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
        })
    }
}
kotlin {
    jvmToolchain(21)

}