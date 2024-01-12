package com.therohankumar.beat.jda

import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class Command(val name: String) {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    suspend fun invoke0(ctx: CommandContext) {
        ctx.apply { invoke() }
    }

    abstract suspend fun CommandContext.invoke()

}