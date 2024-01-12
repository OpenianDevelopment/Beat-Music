package com.therohankumar.beat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class BeatApplication

fun main(args: Array<String>) {
	System.setProperty("spring.config.name", "beat")
	System.setProperty("spring.config.title", "beat")
	runApplication<BeatApplication>(*args)
}
