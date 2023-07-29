package ch.skyfy.manymanycommands.commands.homes

import ch.skyfy.json5configlib.updateIterableNested
import ch.skyfy.manymanycommands.api.data.Home
import ch.skyfy.manymanycommands.api.data.Location
import ch.skyfy.manymanycommands.api.data.Player
import ch.skyfy.manymanycommands.api.persistent.Persistent
import ch.skyfy.manymanycommands.api.utils.getHomesRule
import ch.skyfy.manymanycommands.api.utils.getPlayerNameWithUUID
import ch.skyfy.manymanycommands.commands.AbstractCommand
import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.arguments.DoubleArgumentType.getDouble
import com.mojang.brigadier.arguments.FloatArgumentType.getFloat
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.function.Supplier

fun addHomeToPlayer(
    spe: ServerPlayerEntity,
    homeName: String,
    x: Double = spe.x,
    y: Double = spe.y,
    z: Double = spe.z,
    pitch: Float = spe.pitch,
    yaw: Float = spe.yaw
): Int {

    val player = Persistent.HOMES.serializableData.players.find { getPlayerNameWithUUID(spe) == it.nameWithUUID } ?: return SINGLE_SUCCESS
    val rule = getHomesRule(player.nameWithUUID) ?: return SINGLE_SUCCESS

    if (rule.allowedDimensionCreating.none { dimensionName -> dimensionName == spe.world.dimensionKey.value.toString() }) {
        spe.sendMessage(Text.literal("You don't have the permission to create a home in this dimension !").setStyle(Style.EMPTY.withColor(Formatting.RED)))
        return SINGLE_SUCCESS
    }

    // Check for home duplication
    player.homes.find { homeName == it.name }?.let {
        spe.sendMessage(Text.literal("You already have a home named $homeName").setStyle(Style.EMPTY.withColor(Formatting.RED)))
        return SINGLE_SUCCESS
    }

    // Check for maxHomes rule
    if (player.homes.size + 1 > rule.maxHomes) {
        spe.sendMessage(Text.literal("You can't have more than ${rule.maxHomes} homes").setStyle(Style.EMPTY.withColor(Formatting.RED)))
        return SINGLE_SUCCESS
    }

    Persistent.HOMES.updateIterableNested(Player::homes, player.homes) { it.add(Home(homeName, Location(x, y, z, pitch, yaw, spe.world.dimensionKey.value.toString()))) }

    spe.sendMessage(Text.literal("The home of name «$homeName» at coordinate ${String.format("%.2f", x)} ${String.format("%.2f", y)} ${String.format("%.2f", z)} has been added").setStyle(Style.EMPTY.withColor(Formatting.GREEN)))
    return SINGLE_SUCCESS
}

class CreateHome : AbstractCommand() {
    override fun runImpl(context: CommandContext<ServerCommandSource>): Int {
        return addHomeToPlayer(context.source?.player ?: return SINGLE_SUCCESS, getString(context, "homeName"))
    }
}

class CreateHomeWithCoordinates : AbstractCommand() {
    override fun runImpl(context: CommandContext<ServerCommandSource>): Int {
        return addHomeToPlayer(
            spe = context.source?.player ?: return SINGLE_SUCCESS,
            homeName = getString(context, "homeName"),
            x = getDouble(context, "x"),
            y = getDouble(context, "y"),
            z = getDouble(context, "z"),
            pitch = getFloat(context, "pitch"),
            yaw = getFloat(context, "yaw"),
        )
    }
}

class CreateHomeForAnotherPlayer : AbstractCommand() {
    override fun runImpl(context: CommandContext<ServerCommandSource>): Int {
        val targetPlayerName = getString(context, "playerName")
        val targetPlayer = context.source?.server?.playerManager?.getPlayer(targetPlayerName)
        if (targetPlayer != null) addHomeToPlayer(targetPlayer, getString(context, "homeName"))
        else context.source?.sendFeedback(Supplier { Text.literal("Player not found") }, false)
        return SINGLE_SUCCESS
    }
}

class CreateHomeForAnotherPlayerWithCoordinates : AbstractCommand() {
    override fun runImpl(context: CommandContext<ServerCommandSource>): Int {
        val targetPlayerName = getString(context, "playerName")
        val targetPlayer = context.source?.server?.playerManager?.getPlayer(targetPlayerName)
        if (targetPlayer != null)
            addHomeToPlayer(
                spe = targetPlayer,
                homeName = getString(context, "homeName"),
                x = getDouble(context, "x"),
                y = getDouble(context, "y"),
                z = getDouble(context, "z"),
                pitch = getFloat(context, "pitch"),
                yaw = getFloat(context, "yaw"),
            )
        else context.source?.sendFeedback(Supplier { Text.literal("Player not found") }, false)
        return SINGLE_SUCCESS
    }
}