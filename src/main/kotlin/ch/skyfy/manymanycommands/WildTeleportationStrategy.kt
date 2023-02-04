package ch.skyfy.manymanycommands

import ch.skyfy.json5configlib.updateMap
import ch.skyfy.manymanycommands.api.CustomTeleportationStrategy
import ch.skyfy.manymanycommands.api.config.WildRule
import ch.skyfy.manymanycommands.api.data.Location
import ch.skyfy.manymanycommands.api.data.Player
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

    override fun getLocation(context: CommandContext<ServerCommandSource>, spe: ServerPlayerEntity, player: Player): Location? {

        if (rule.allowedDimension.none { allowedDim -> allowedDim == spe.world.dimensionKey.value.toString() }) {
            spe.sendMessage(Text.literal("You cannot use this command in this dimension").setStyle(Style.EMPTY.withColor(Formatting.RED)))
            return null
        }

        return when (WildCmd.Type.valueOf(StringArgumentType.getString(context, "type"))) {
            WildCmd.Type.DIRECT -> WildCmd.getRandomLocation()
            WildCmd.Type.TIMED -> Persistent.OTHERS_DATA.serializableData.wildTimedLocation
        }
    }

    override fun check(spe: ServerPlayerEntity, player: Player): Boolean {
        Persistent.OTHERS_DATA.serializableData.currentUsageOfWildCommand[player.nameWithUUID]?.let { currentUsageOfWildCommand ->
            if (currentUsageOfWildCommand > rule.maximumUsage) {
                spe.sendMessage(Text.literal("You have reached the maximum number of uses for this command").setStyle(Style.EMPTY.withColor(Formatting.RED)))
                return false
            }
        }
        return true
    }

    override fun onTeleportDone(spe: ServerPlayerEntity, previousLocation: Location) {
        spe.addStatusEffect(StatusEffectInstance(StatusEffects.RESISTANCE, 20 * 20, 9))
        spe.addStatusEffect(StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 20 * 20, 9))
        spe.sendMessage(Text.literal("You have been teleported to an unknown lands, Good Adventure ! ").setStyle(Style.EMPTY.withColor(Formatting.GREEN)))
        Persistent.OTHERS_DATA.updateMap(OthersData::currentUsageOfWildCommand) { currentUsageOfWildCommand ->
            currentUsageOfWildCommand.merge(getPlayerNameWithUUID(spe), 1, Int::plus)
        }
    }
}