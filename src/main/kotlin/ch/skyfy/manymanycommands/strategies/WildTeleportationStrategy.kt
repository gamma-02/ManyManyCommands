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

class WildTeleportationStrategy(override val rule: WildRule) : CustomTeleportationStrategy<WildRule>() {

    override fun getLocation(context: CommandContext<ServerCommandSource>, spe: ServerPlayerEntity): Location? {
        if (rule.allowedDimension.none { allowedDim -> allowedDim == spe.world.dimensionKey.value.toString() }) {
            spe.sendMessage(Text.literal("You cannot use this command in this dimension").setStyle(Style.EMPTY.withColor(Formatting.RED)))
            return null
        }

        val loc = when (WildCmd.Type.valueOf(StringArgumentType.getString(context, "type"))) {
            WildCmd.Type.DIRECT -> WildCmd.getRandomLocation()
            WildCmd.Type.TIMED -> Persistent.OTHERS_DATA.serializableData.wildTimedLocation
        } ?: return null
        return Location(loc.x, loc.y, loc.z, loc.pitch, loc.yaw, spe.world.dimensionKey.value.toString())
    }

    override fun check(spe: ServerPlayerEntity): Boolean {
        Persistent.OTHERS_DATA.serializableData.currentUsageOfWildCommand[getPlayerNameWithUUID(spe)]?.let { currentUsageOfWildCommand ->
            if (currentUsageOfWildCommand > rule.maximumUsage) {
                spe.sendMessage(Text.literal("You have reached the maximum number of uses for this command").setStyle(Style.EMPTY.withColor(Formatting.RED)))
                return false
            }
        }
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