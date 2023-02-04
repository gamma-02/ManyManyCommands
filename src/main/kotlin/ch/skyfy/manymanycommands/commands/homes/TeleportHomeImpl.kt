package ch.skyfy.manymanycommands.commands.homes

import ch.skyfy.manymanycommands.api.config.HomesRule
import ch.skyfy.manymanycommands.api.data.Home
import ch.skyfy.manymanycommands.api.data.Location
import ch.skyfy.manymanycommands.api.data.Player
import ch.skyfy.manymanycommands.api.data.Teleportation
import ch.skyfy.manymanycommands.api.persistent.Persistent
import ch.skyfy.manymanycommands.api.utils.getHomesRule
import ch.skyfy.manymanycommands.api.utils.getPlayerNameWithUUID
import ch.skyfy.manymanycommands.commands.AbstractTeleportation
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

abstract class TeleportHomeImpl : AbstractTeleportation(Teleportation.homesTeleporting, Teleportation.homesCooldowns) {

    private var home: Home? = null
    private var homesRule: HomesRule? = null

    override fun getRule(player: Player): TeleportationRule? {
        val rule = getHomesRule(player) ?: return null
        this.homesRule = rule
        return TeleportationRule(rule.cooldown, rule.standStill)
    }

    override fun getLocation(context: CommandContext<ServerCommandSource>, spe: ServerPlayerEntity, player: Player): Location? {
        val homeName = getString(context, "homeName")
        val home = player.homes.find { it.name == homeName }
        if (home == null) {
            context.source.player?.sendMessage(Text.literal("Home $homeName does not exist !").setStyle(Style.EMPTY.withColor(Formatting.RED)))
            return null
        }
        this.home = home
        return home.location
    }

    override fun check(spe: ServerPlayerEntity, player: Player): Boolean {
        homesRule?.let { homesRules ->
            if (homesRules.allowedDimensionTeleporting.none { it == spe.world.dimensionKey.value.toString() }) {
                spe.sendMessage(Text.literal("You cannot use this command in this dimension !").setStyle(Style.EMPTY.withColor(Formatting.RED)))
                return false
            }
        }
        return true
    }

    override fun onTeleport(spe: ServerPlayerEntity) {
        Persistent.OTHERS_DATA.serializableData.previousLocation[getPlayerNameWithUUID(spe)] = Location(spe.x, spe.y, spe.z, spe.yaw, spe.pitch, spe.world.dimensionKey.value.toString())
        spe.sendMessage(Text.literal("You've arrived at your destination (${home?.name})").setStyle(Style.EMPTY.withColor(Formatting.GREEN)))
    }
}

class TeleportHome : TeleportHomeImpl() {
    override fun runImpl(context: CommandContext<ServerCommandSource>): Int {
        if (context.source.player !is ServerPlayerEntity) return Command.SINGLE_SUCCESS
        return super.runImpl(context)
    }
}

class TeleportHomeToAnotherPlayer : TeleportHomeImpl() {
    override fun runImpl(context: CommandContext<ServerCommandSource>): Int {
        val targetPlayer = context.source?.server?.playerManager?.getPlayer(getString(context, "playerName"))
        if (targetPlayer != null) super.runImpl(context)
        else context.source?.sendFeedback(Text.literal("Player not found"), false)
        return Command.SINGLE_SUCCESS
    }
}