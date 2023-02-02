package ch.skyfy.manymanycommands.commands.homes

import ch.skyfy.manymanycommands.api.config.Configs
import ch.skyfy.manymanycommands.commands.AbstractCommand
import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.context.CommandContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

fun listHome(
    playerEntity: PlayerEntity
): Int {
    val player = Configs.PLAYERS_CONFIG.serializableData.players.find { playerEntity.uuidAsString == it.uuid } ?: return SINGLE_SUCCESS
    player.homes.forEach {
        playerEntity.sendMessage(Text.literal("- ${it.name}").setStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE)))
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