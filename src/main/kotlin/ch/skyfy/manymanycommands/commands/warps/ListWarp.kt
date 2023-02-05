package ch.skyfy.manymanycommands.commands.warps

import ch.skyfy.manymanycommands.api.persistent.Persistent
import ch.skyfy.manymanycommands.api.utils.getPlayerNameWithUUID
import ch.skyfy.manymanycommands.api.utils.getWarps
import ch.skyfy.manymanycommands.commands.AbstractCommand
import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class ListWarp : AbstractCommand() {
    override fun runImpl(context: CommandContext<ServerCommandSource>): Int {
        if (context.source.player !is ServerPlayerEntity) return 1
        val spe = context.source.player!!

        Persistent.HOMES.serializableData.players.firstOrNull { it.nameWithUUID == getPlayerNameWithUUID(spe) }?.let { player ->
            getWarps(player.nameWithUUID).forEach { warp ->
                spe.sendMessage(Text.literal("- ${warp.name}").setStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE)))
            }
        }

        return Command.SINGLE_SUCCESS
    }
}