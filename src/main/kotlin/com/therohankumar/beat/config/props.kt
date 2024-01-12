package com.therohankumar.beat.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("config")
class props {
    var token: String = ""
    var shards: Int = 1
    var artist: String = "Guitar"
}