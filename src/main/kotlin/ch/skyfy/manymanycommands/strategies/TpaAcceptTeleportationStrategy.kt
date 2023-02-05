package ch.skyfy.manymanycommands.strategies

import ch.skyfy.json5configlib.updateMap
import ch.skyfy.manymanycommands.api.CustomTeleportationStrategy
import ch.skyfy.manymanycommands.api.config.TpaRule
import ch.skyfy.manymanycommands.api.data.Location
import ch.skyfy.manymanycommands.api.persistent.OthersData
import ch.skyfy.manymanycommands.api.persistent.Persistent
import ch.skyfy.manymanycommands.api.utils.getPlayerNameWithUUID
import ch.skyfy.manymanycommands.commands.tpa.TpaCmd
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity

class TpaAcceptTeleportationStrategy(
    private val requestType: TpaCmd.RequestType,
    private val otherPlayer: ServerPlayerEntity,
    private val playerToTeleport: ServerPlayerEntity,
    override val rule: TpaRule
) : CustomTeleportationStrategy<TpaRule>() {

    override fun getPlayerToTeleport(context: CommandContext<ServerCommandSource>, spe: ServerPlayerEntity) = playerToTeleport

    override fun getLocation(context: CommandContext<ServerCommandSource>, spe: ServerPlayerEntity) = Location(otherPlayer.x, otherPlayer.y, otherPlayer.z, otherPlayer.pitch, otherPlayer.yaw, otherPlayer.world.dimensionKey.value.toString())

//    override fun check(spe: ServerPlayerEntity): Boolean {
//        if (rule.allowedDimension.none { it == spe.world.dimensionKey.value.toString() }) {
//            spe.sendMessage(Text.literal("You cannot use this command in this dimension !").setStyle(Style.EMPTY.withColor(Formatting.RED)))
//            return false
//        }
//        return true
//    }

    override fun check(spe: ServerPlayerEntity) = true

    override fun onTeleportDone(spe: ServerPlayerEntity, previousLocation: Location) {
        Persistent.OTHERS_DATA.updateMap(OthersData::previousLocation) { it[getPlayerNameWithUUID(spe)] = previousLocation }
        when (requestType) {
            TpaCmd.RequestType.REQUEST -> TpaCmd.RECEIVED_REQUESTS[otherPlayer.name.string]?.removeIf { it.first == playerToTeleport.name.string }
            TpaCmd.RequestType.REQUEST_HERE -> TpaCmd.RECEIVED_REQUESTS[playerToTeleport.name.string]?.removeIf { it.first == otherPlayer.name.string }
        }
    }
}