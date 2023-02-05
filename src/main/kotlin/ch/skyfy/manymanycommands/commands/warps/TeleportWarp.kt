package ch.skyfy.manymanycommands.commands.warps

import ch.skyfy.manymanycommands.api.CustomTeleportationStrategy
import ch.skyfy.manymanycommands.api.config.WarpRule
import ch.skyfy.manymanycommands.api.data.Teleportation
import ch.skyfy.manymanycommands.api.persistent.Persistent
import ch.skyfy.manymanycommands.api.utils.getPlayerNameWithUUID
import ch.skyfy.manymanycommands.api.utils.getWarpRule
import ch.skyfy.manymanycommands.api.utils.getWarps
import ch.skyfy.manymanycommands.commands.AbstractTeleportation
import ch.skyfy.manymanycommands.strategies.WarpsTeleportationStrategy
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class TeleportWarp : AbstractTeleportation<WarpRule>(Teleportation.warpsTeleporting, Teleportation.warpsCooldowns) {

    override fun runStrategy(context: CommandContext<ServerCommandSource>, spe: ServerPlayerEntity): CustomTeleportationStrategy<*>? {
        val warpName = getString(context, "warpName")

        val warp = Persistent.WARPS.serializableData.warps.find { it.name == warpName }
        if (warp == null) {
            context.source.player?.sendMessage(Text.literal("Warp $warpName does not exist !").setStyle(Style.EMPTY.withColor(Formatting.RED)))
            return null
        }

        if (getWarps(getPlayerNameWithUUID(spe)).none { it.name == warp.name }) {
            context.source.player?.sendMessage(Text.literal("Warp $warpName exist, but you don't have the privilege to use it").setStyle(Style.EMPTY.withColor(Formatting.GOLD)))
            return null
        }

        val rule = getWarpRule(getPlayerNameWithUUID(spe)) ?: return null

        return WarpsTeleportationStrategy(warpName, warp, rule)
    }

}