package ch.skyfy.manymanycommands.strategies

import ch.skyfy.json5configlib.updateMap
import ch.skyfy.manymanycommands.api.CustomTeleportationStrategy
import ch.skyfy.manymanycommands.api.config.HomesRule
import ch.skyfy.manymanycommands.api.data.CommandType
import ch.skyfy.manymanycommands.api.data.Home
import ch.skyfy.manymanycommands.api.data.Location
import ch.skyfy.manymanycommands.api.events.PlayerTeleportationEvents
import ch.skyfy.manymanycommands.api.persistent.OthersData
import ch.skyfy.manymanycommands.api.persistent.Persistent
import ch.skyfy.manymanycommands.api.utils.getPlayerNameWithUUID
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class HomesTeleportationStrategy(private val homeName: String, private val home: Home, override val rule: HomesRule) : CustomTeleportationStrategy<HomesRule>() {

    override fun getLocation(context: CommandContext<ServerCommandSource>, spe: ServerPlayerEntity) = home.location

    override fun check(spe: ServerPlayerEntity): Boolean {
        if (rule.allowedDimensionTeleporting.none { it == spe.world.dimensionEntry.value().toString() }) {
            spe.sendMessage(Text.literal("You cannot use this command in this dimension !").setStyle(Style.EMPTY.withColor(Formatting.RED)))
            return false
        }
        return true
    }

    override fun onTeleportDone(spe: ServerPlayerEntity, previousLocation: Location) {
        Persistent.OTHERS_DATA.updateMap(OthersData::previousLocation) { it[getPlayerNameWithUUID(spe)] = previousLocation }
        spe.sendMessage(Text.literal("You've arrived at your destination ($homeName)").setStyle(Style.EMPTY.withColor(Formatting.GREEN)))
        PlayerTeleportationEvents.TELEPORTATION_DONE.invoker().onTeleportationDone(spe, rule, CommandType.HOMES)
    }
}