package com.therohankumar

import io.github.cdimascio.dotenv.dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class BeatBotApplication

fun main(args: Array<String>) {
    // Load .env for local development; ignored in production where real env vars are set
    val dotenv = dotenv {
        ignoreIfMissing = true
        ignoreIfMalformed = true
    }
    dotenv.entries().forEach { entry ->
        if (System.getenv(entry.key) == null) {
            System.setProperty(entry.key, entry.value)
        }
    }
    runApplication<BeatBotApplication>(*args)
}
