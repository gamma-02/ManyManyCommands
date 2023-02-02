package ch.skyfy.manymanycommands.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource

abstract class AbstractCommand : Command<ServerCommandSource> {

    override fun run(context: CommandContext<ServerCommandSource>): Int {
        return runImpl(context)
    }

    abstract fun runImpl(context: CommandContext<ServerCommandSource>): Int
}