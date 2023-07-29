package ch.skyfy.manymanycommands.commands.warps

import ch.skyfy.manymanycommands.api.CustomTeleportationStrategy
import ch.skyfy.manymanycommands.api.config.WarpRule
import ch.skyfy.manymanycommands.api.data.Teleportation
import ch.skyfy.manymanycommands.api.persistent.Persistent
import ch.skyfy.manymanycommands.api.utils.getPlayerNameWithUUID
import ch.skyfy.manymanycommands.api.utils.getWarpRule
import ch.skyfy.manymanycommands.api.utils.getWarps
import ch.skyfy.manymanycommands.commands.AbstractTeleportation
import ch.skyfy.manymanycommands.commands.homes.TeleportHomeImpl
import ch.skyfy.manymanycommands.strategies.WarpsTeleportationStrategy
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.function.Supplier

open class TeleportWarpsImpl : AbstractTeleportation<WarpRule>(Teleportation.warpsTeleporting, Teleportation.warpsCooldowns) {
    override fun runStrategy(context: CommandContext<ServerCommandSource>, spe: ServerPlayerEntity): CustomTeleportationStrategy<*>? {
        val warpName = StringArgumentType.getString(context, "warpName")

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

class TeleportWarps : TeleportWarpsImpl()

class TeleportWarpsForAnotherPlayer: TeleportWarpsImpl() {
    override fun runImpl(context: CommandContext<ServerCommandSource>): Int {
        val targetPlayer = context.source?.server?.playerManager?.getPlayer(StringArgumentType.getString(context, "playerName"))
        if (targetPlayer != null) super.runImpl(context)
        else context.source?.sendFeedback(Supplier { Text.literal("Player not found") }, false)
        return Command.SINGLE_SUCCESS
    }
}