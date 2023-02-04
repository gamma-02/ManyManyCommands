package ch.skyfy.manymanycommands.commands.homes

import ch.skyfy.manymanycommands.api.persistent.Persistent
import ch.skyfy.manymanycommands.api.utils.getPlayerNameWithUUID
import ch.skyfy.manymanycommands.commands.AbstractCommand
import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

fun listHome(
    spe: ServerPlayerEntity
): Int {
    val player = Persistent.HOMES.serializableData.players.find { getPlayerNameWithUUID(spe) == it.nameWithUUID } ?: return SINGLE_SUCCESS
    player.homes.forEach {
        spe.sendMessage(Text.literal("- ${it.name}").setStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE)))
    }
    return SINGLE_SUCCESS
}

class ListHome : AbstractCommand() {
    override fun runImpl(context: CommandContext<ServerCommandSource>): Int {
        return listHome(context.source?.player ?: return SINGLE_SUCCESS)
    }
}

class ListHomeForAnotherPlayer : AbstractCommand() {
    override fun runImpl(context: CommandContext<ServerCommandSource>): Int {
        val targetPlayerName = getString(context, "playerName")
        val targetPlayer = context.source?.server?.playerManager?.getPlayer(targetPlayerName)
        if (targetPlayer != null) listHome(targetPlayer)
        else context.source?.sendFeedback(Text.literal("Player not found"), false)
        return SINGLE_SUCCESS
    }
}