package ch.skyfy.manymanycommands.commands.warps

import ch.skyfy.manymanycommands.api.config.Configs
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

        Configs.PLAYERS.serializableData.players.firstOrNull { it.nameWithUUID == getPlayerNameWithUUID(spe) }?.let { player ->

//            Configs.WARPS.serializableData.groups.filter { it.name }

//            val playerGroups = Configs.PLAYERS_CONFIG.serializableData.playerGroups.filter { playerGroup ->
//                playerGroup.players.any { it.uuid == player.uuid }
//            }
//
//            val w = Configs.WARPS.serializableData.warps.map { warp ->
//                Configs.WARPS.serializableData.groups.any { warpGroup ->
//                    playerGroups.any { playerGroup ->
//                        playerGroup.warpGroups.any { it == warpGroup.name } } && warpGroup.warps.any { it == warp.name }
//                }
//                warp
//            }

            getWarps(player).forEach { warp ->
                spe.sendMessage(Text.literal("- ${warp.name}").setStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE)))
            }

        }

//        context.source.player?.let {
//
//            Configs.WARPS.serializableData.warps.forEach { warp ->
//                it.sendMessage(Text.literal("- ${warp.name}").setStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE)))
//            }
//        }
        return Command.SINGLE_SUCCESS
    }
}