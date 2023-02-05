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
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.concurrent.atomic.AtomicBoolean

class TpaAcceptTeleportationStrategy(
    private val requestType: TpaCmd.RequestType,
    private val otherPlayer: ServerPlayerEntity,
    private val playerToTeleport: ServerPlayerEntity,
    private val playerWhoRequested: ServerPlayerEntity,
    override val rule: TpaRule
) : CustomTeleportationStrategy<TpaRule>() {

    override fun getPlayerToTeleport(context: CommandContext<ServerCommandSource>, spe: ServerPlayerEntity) = playerToTeleport

    override fun getLocation(context: CommandContext<ServerCommandSource>, spe: ServerPlayerEntity) = Location(otherPlayer.x, otherPlayer.y, otherPlayer.z, otherPlayer.pitch, otherPlayer.yaw, otherPlayer.world.dimensionKey.value.toString())

    override fun check(spe: ServerPlayerEntity): Boolean {
        val shouldCancel = AtomicBoolean(false)
        Persistent.OTHERS_DATA.updateMap(OthersData::tpaAcceptUsagePerTime) { tpaAcceptUsagePerTime ->
            tpaAcceptUsagePerTime.compute(getPlayerNameWithUUID(playerWhoRequested)) { _, value ->
                if (value == null) return@compute Pair(System.currentTimeMillis(), 1)
                else {
                    val elapsedTime = System.currentTimeMillis() - value.first

                    if (value.second >= rule.maximumUsageInTotal) {
                        playerWhoRequested.sendMessage(Text.literal("You have reached the total limit of a teleportation with /tpa").setStyle(Style.EMPTY.withColor(Formatting.RED)))
                        shouldCancel.set(true)
                        return@compute value
                    }

                    val totalSecs = rule.maximumUsagePerSpecificTime.time
                    val hours = totalSecs / 3600
                    val minutes = (totalSecs % 3600) / 60
                    val seconds = totalSecs % 60
                    val timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)

                    val totalSecsRemaining = totalSecs - (elapsedTime / 1000)
                    val hours2 = totalSecsRemaining / 3600
                    val minutes2 = (totalSecsRemaining % 3600) / 60
                    val seconds2 = totalSecsRemaining % 60
                    val timeString2 = String.format("%02d:%02d:%02d", hours2, minutes2, seconds2)

                    if ((elapsedTime / 1000) <= rule.maximumUsagePerSpecificTime.time) {
                        if (value.second < rule.maximumUsagePerSpecificTime.maximumUsage) {
                            return@compute Pair(value.first, value.second + 1)
                        } else {
                            playerWhoRequested.sendMessage(Text.literal("You can only be teleported with /tpa ${rule.maximumUsagePerSpecificTime.maximumUsage} times every $timeString. You've reached the limit !").setStyle(Style.EMPTY.withColor(Formatting.RED)))
                            playerWhoRequested.sendMessage(Text.literal("Time to wait before using teleportation again $timeString2").setStyle(Style.EMPTY.withColor(Formatting.RED)))
                            shouldCancel.set(true)
                            return@compute value
                        }
                    } else return@compute Pair(System.currentTimeMillis(), 1)

                }
            }
        }

        if (shouldCancel.get()) return false

        return true
    }

    override fun onTeleportDone(spe: ServerPlayerEntity, previousLocation: Location) {
        Persistent.OTHERS_DATA.updateMap(OthersData::previousLocation) { it[getPlayerNameWithUUID(spe)] = previousLocation }
        when (requestType) {
            TpaCmd.RequestType.REQUEST -> TpaCmd.RECEIVED_REQUESTS[otherPlayer.name.string]?.removeIf { it.first == playerToTeleport.name.string }
            TpaCmd.RequestType.REQUEST_HERE -> TpaCmd.RECEIVED_REQUESTS[playerToTeleport.name.string]?.removeIf { it.first == otherPlayer.name.string }
        }
    }
}