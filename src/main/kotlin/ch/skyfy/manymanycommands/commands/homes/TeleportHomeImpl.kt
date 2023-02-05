package ch.skyfy.manymanycommands.commands.homes

import ch.skyfy.manymanycommands.api.CustomTeleportationStrategy
import ch.skyfy.manymanycommands.api.config.HomesRule
import ch.skyfy.manymanycommands.api.data.Teleportation
import ch.skyfy.manymanycommands.api.persistent.Persistent
import ch.skyfy.manymanycommands.api.utils.getHomesRule
import ch.skyfy.manymanycommands.api.utils.getPlayerNameWithUUID
import ch.skyfy.manymanycommands.commands.AbstractTeleportation
import ch.skyfy.manymanycommands.strategies.HomesTeleportationStrategy
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

abstract class TeleportHomeImpl : AbstractTeleportation<HomesRule>(Teleportation.homesTeleporting, Teleportation.homesCooldowns) {

    override fun runStrategy(context: CommandContext<ServerCommandSource>, spe: ServerPlayerEntity): CustomTeleportationStrategy<*>? {
        val homeName = getString(context, "homeName")
        val player = Persistent.HOMES.serializableData.players.find { getPlayerNameWithUUID(spe) == it.nameWithUUID } ?: return null
        val home = player.homes.find { it.name == homeName }

        if (home == null) {
            context.source.player?.sendMessage(Text.literal("Home $homeName does not exist !").setStyle(Style.EMPTY.withColor(Formatting.RED)))
            return null
        }

        val rule = getHomesRule(player.nameWithUUID) ?: return null

        return HomesTeleportationStrategy(homeName, home, rule)
    }

}

class TeleportHome : TeleportHomeImpl()

class TeleportHomeToAnotherPlayer : TeleportHomeImpl() {
    override fun runImpl(context: CommandContext<ServerCommandSource>): Int {
        val targetPlayer = context.source?.server?.playerManager?.getPlayer(getString(context, "playerName"))
        if (targetPlayer != null) super.runImpl(context)
        else context.source?.sendFeedback(Text.literal("Player not found"), false)
        return Command.SINGLE_SUCCESS
    }
}