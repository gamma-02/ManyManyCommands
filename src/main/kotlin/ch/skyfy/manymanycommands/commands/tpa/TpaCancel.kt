package ch.skyfy.manymanycommands.commands.tpa

import ch.skyfy.manymanycommands.commands.AbstractCommand
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity

class TpaCancel : AbstractCommand() {
    override fun runImpl(context: CommandContext<ServerCommandSource>): Int {
        if (context.source.player !is ServerPlayerEntity) return Command.SINGLE_SUCCESS

        val spe = context.source.player!!

        val playerName = StringArgumentType.getString(context, "playerName")

        TpaCmd.RECEIVED_REQUESTS[playerName]?.let { pairs -> pairs.removeIf { it.first == spe.name.string } }

        return Command.SINGLE_SUCCESS
    }

}