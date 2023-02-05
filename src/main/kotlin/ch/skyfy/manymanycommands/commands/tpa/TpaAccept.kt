package ch.skyfy.manymanycommands.commands.tpa

import ch.skyfy.manymanycommands.api.CustomTeleportationStrategy
import ch.skyfy.manymanycommands.api.config.TpaRule
import ch.skyfy.manymanycommands.api.data.Teleportation
import ch.skyfy.manymanycommands.api.utils.getPlayerNameWithUUID
import ch.skyfy.manymanycommands.api.utils.getTpaAcceptRule
import ch.skyfy.manymanycommands.commands.AbstractTeleportation
import ch.skyfy.manymanycommands.strategies.TpaAcceptTeleportationStrategy
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class TpaAccept : AbstractTeleportation<TpaRule>(Teleportation.tpaAcceptTeleporting, Teleportation.tpaAcceptCooldowns) {
    override fun runStrategy(context: CommandContext<ServerCommandSource>, spe: ServerPlayerEntity): CustomTeleportationStrategy<*>? {
        val playerName = StringArgumentType.getString(context, "playerName")
        val playerTarget = context.source.server.playerManager.getPlayer(playerName)

        val rule = getTpaAcceptRule(getPlayerNameWithUUID(spe)) ?: return null

        val list = TpaCmd.RECEIVED_REQUESTS[spe.name.string]

        if (list == null || list.none { pair -> pair.first == playerName }) {
            spe.sendMessage(Text.literal("Player $playerName didn't send you a teleportation request").setStyle(Style.EMPTY.withColor(Formatting.RED)))
            return null
        } else {
            if (playerTarget == null) {
                spe.sendMessage(Text.literal("The player who asked you for a teleportation request is no longer connected").setStyle(Style.EMPTY.withColor(Formatting.RED)))
                return null
            }

            list.firstOrNull { pair -> pair.first == playerName }?.let { pair ->
                return when (pair.second) {
                    TpaCmd.RequestType.REQUEST -> TpaAcceptTeleportationStrategy(pair.second, spe, playerTarget, playerTarget, rule)
                    TpaCmd.RequestType.REQUEST_HERE -> TpaAcceptTeleportationStrategy(pair.second, playerTarget, spe, playerTarget, rule)
                }
            }
        }

        return null
    }
}