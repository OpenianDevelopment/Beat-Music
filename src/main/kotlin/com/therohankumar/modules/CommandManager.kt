package com.therohankumar.modules

import com.therohankumar.commands.*
import com.therohankumar.interfaces.ICommand
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

object CommandManager {
    private val commands = mutableMapOf<String, ICommand>()
    private var slashCommands = listOf<SlashCommandData>()
    private val commandList = listOf(
        Play(),
        Filter(),
        Skip(),
        Stop(),
        Queue(),
        NowPlaying(),
        Pause(),
        Resume(),
        Clear(),
        Shuffle(),
        Shift()
    )


    fun registerAllCommand() {
        commandList.forEach { command ->
            commands[command.name] = command
            slashCommands += command.createSlashCommand()
        }
    }

    fun getCommand(name: String): ICommand? {
        return commands[name]
    }

    fun getAllSlashCommand(): List<SlashCommandData> {
        return slashCommands
    }
}