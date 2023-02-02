package ch.skyfy.manymanycommands.commands.warps

import ch.skyfy.manymanycommands.api.config.Configs
import ch.skyfy.manymanycommands.api.config.Player
import ch.skyfy.manymanycommands.api.config.Warp
import ch.skyfy.manymanycommands.api.data.Location
import ch.skyfy.manymanycommands.api.data.Teleportation
import ch.skyfy.manymanycommands.api.persistent.Persistent
import ch.skyfy.manymanycommands.api.utils.getWarpRule
import ch.skyfy.manymanycommands.api.utils.getWarps
import ch.skyfy.manymanycommands.commands.AbstractTeleportation
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class TeleportWarp : AbstractTeleportation(Teleportation.warpsTeleporting, Teleportation.warpsCooldowns) {

    private var warp: Warp? = null

    override fun onTeleport(spe: ServerPlayerEntity) {
        Persistent.PERSISTENT_DATA.serializableData.previousLocation[spe.uuidAsString] = Location(spe.x, spe.y, spe.z, spe.yaw, spe.pitch, spe.world.dimensionKey.value.toString())
        spe.sendMessage(Text.literal("You've arrived at your destination (${warp?.name})").setStyle(Style.EMPTY.withColor(Formatting.GREEN)))
    }

    override fun getLocation(context: CommandContext<ServerCommandSource>, player: Player): Location? {
        val warpName = getString(context, "warpName")

        getWarps(player)

        val warp = Configs.WARPS.serializableData.warps.find { it.name == warpName }
        if (warp == null) {
            context.source.player?.sendMessage(Text.literal("Warp $warpName does not exist !").setStyle(Style.EMPTY.withColor(Formatting.RED)))
            return null
        }

        if(getWarps(player).none { it.name == warp.name }){
            context.source.player?.sendMessage(Text.literal("Warp $warpName exist, but you don't have the privilege to use it").setStyle(Style.EMPTY.withColor(Formatting.GOLD)))
            return null
        }

        this.warp = warp
        return warp.location
    }

    override fun getRule(player: Player): TeleportationRule? {
        val rule = getWarpRule(player) ?: return null
        return TeleportationRule(rule.cooldown, rule.standStill)
    }
}