package ch.skyfy.manymanycommands.commands.tpa

import ch.skyfy.manymanycommands.api.utils.getPlayerNameWithUUID
import ch.skyfy.manymanycommands.api.utils.getTpaAcceptRule
import ch.skyfy.manymanycommands.commands.AbstractCommand
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

abstract class TpaRequestBase : AbstractCommand() {

    abstract fun customMessage(playerName: String): Text

    override fun runImpl(context: CommandContext<ServerCommandSource>): Int {
        if (context.source.player !is ServerPlayerEntity) return Command.SINGLE_SUCCESS

        val spe = context.source.player!!
        val server = context.source.server

        val rule = getTpaAcceptRule(getPlayerNameWithUUID(spe)) ?: return Command.SINGLE_SUCCESS

        val playerName = StringArgumentType.getString(context, "playerName")
        val targetPlayer = server.playerManager.getPlayer(playerName)

        if (rule.allowedDimension.none { it == spe.world.dimensionEntry.key.get().value.toString() }) {
            spe.sendMessage(Text.literal("You are not allowed to use this command in this dimension !").setStyle(Style.EMPTY.withColor(Formatting.RED)))
            return Command.SINGLE_SUCCESS
        }

        if (targetPlayer == null) {
            spe.sendMessage(Text.literal("The player you want to teleport to is no longer on the server").setStyle(Style.EMPTY.withColor(Formatting.RED)))
            return Command.SINGLE_SUCCESS
        }

        TpaCmd.RECEIVED_REQUESTS.compute(targetPlayer.name.string) { _, set ->
            if (set == null) {
                return@compute mutableSetOf(Pair(spe.name.string, TpaCmd.RequestType.REQUEST))
            } else {
                set.removeIf { it.first == spe.name.string }
                set.add(Pair(spe.name.string, TpaCmd.RequestType.REQUEST))
            }
            return@compute set
        }

        targetPlayer.sendMessage(customMessage(spe.name.string))
        spe.sendMessage(Text.literal("You have sent a teleportation request to ${targetPlayer.name.string}").setStyle(Style.EMPTY.withColor(Formatting.GOLD)))

        return Command.SINGLE_SUCCESS
    }
}