package com.therohankumar.commands

import org.springframework.stereotype.Component

@Component
class CommandRegistry(commands: List<SlashCommand>) {
    private val registry: Map<String, SlashCommand> = commands.associateBy { it.name }

    operator fun get(name: String): SlashCommand? = registry[name]

    val all: Collection<SlashCommand> get() = registry.values
}
