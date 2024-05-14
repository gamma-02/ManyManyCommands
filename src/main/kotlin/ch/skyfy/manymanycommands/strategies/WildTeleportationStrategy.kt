package ch.skyfy.manymanycommands.strategies

import ch.skyfy.json5configlib.updateMap
import ch.skyfy.manymanycommands.api.CustomTeleportationStrategy
import ch.skyfy.manymanycommands.api.config.WildRule
import ch.skyfy.manymanycommands.api.data.CommandType
import ch.skyfy.manymanycommands.api.data.Location
import ch.skyfy.manymanycommands.api.events.PlayerTeleportationEvents
import ch.skyfy.manymanycommands.api.persistent.OthersData
import ch.skyfy.manymanycommands.api.persistent.Persistent
import ch.skyfy.manymanycommands.api.utils.getPlayerNameWithUUID
import ch.skyfy.manymanycommands.commands.WildCmd
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.concurrent.atomic.AtomicBoolean

class WildTeleportationStrategy(override val rule: WildRule) : CustomTeleportationStrategy<WildRule>() {

    override fun getLocation(context: CommandContext<ServerCommandSource>, spe: ServerPlayerEntity): Location? {
        if (rule.allowedDimension.none { allowedDim -> allowedDim == spe.world.dimensionEntry.value().toString() }) {
            spe.sendMessage(Text.literal("You cannot use this command in this dimension").setStyle(Style.EMPTY.withColor(Formatting.RED)))
            return null
        }

        val loc = when (WildCmd.Type.valueOf(StringArgumentType.getString(context, "type"))) {
            WildCmd.Type.DIRECT -> WildCmd.getRandomLocation()
            WildCmd.Type.TIMED -> Persistent.OTHERS_DATA.serializableData.wildTimedLocation
        } ?: return null
        return Location(loc.x, loc.y, loc.z, loc.pitch, loc.yaw, spe.world.dimensionEntry.value().toString())
    }

    override fun check(spe: ServerPlayerEntity): Boolean {
        val shouldCancel = AtomicBoolean(false)
        Persistent.OTHERS_DATA.updateMap(OthersData::wildUsagePerTime) { wildUsagePerTime ->
            wildUsagePerTime.compute(getPlayerNameWithUUID(spe)) { _, value ->
                if (value == null) return@compute Pair(System.currentTimeMillis(), 1)
                else {
                    val elapsedTime = System.currentTimeMillis() - value.first

                    if (value.second >= rule.maximumUsageInTotal) {
                        spe.sendMessage(Text.literal("You have reached the total limit of a teleportation with /wild").setStyle(Style.EMPTY.withColor(Formatting.RED)))
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
                            spe.sendMessage(Text.literal("You can only be teleported with /wild ${rule.maximumUsagePerSpecificTime.maximumUsage} times every $timeString. You've reached the limit !").setStyle(Style.EMPTY.withColor(Formatting.RED)))
                            spe.sendMessage(Text.literal("Time to wait before using teleportation again $timeString2").setStyle(Style.EMPTY.withColor(Formatting.RED)))
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
        spe.addStatusEffect(StatusEffectInstance(StatusEffects.RESISTANCE, 20 * 20, 20))
        spe.addStatusEffect(StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 20 * 20, 2))
        spe.sendMessage(Text.literal("You have been teleported to an unknown lands, Good Adventure ! ").setStyle(Style.EMPTY.withColor(Formatting.GREEN)))
        Persistent.OTHERS_DATA.updateMap(OthersData::previousLocation) { it[getPlayerNameWithUUID(spe)] = previousLocation }
        Persistent.OTHERS_DATA.updateMap(OthersData::currentUsageOfWildCommand) { currentUsageOfWildCommand ->
            currentUsageOfWildCommand.merge(getPlayerNameWithUUID(spe), 1, Int::plus)
        }
        PlayerTeleportationEvents.TELEPORTATION_DONE.invoker().onTeleportationDone(spe, rule, CommandType.WILD)
    }
}