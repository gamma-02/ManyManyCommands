package ch.skyfy.manymanycommands.commands.warps

import ch.skyfy.json5configlib.updateIterable
import ch.skyfy.manymanycommands.api.config.Configs
import ch.skyfy.manymanycommands.api.config.WarpConfig
import ch.skyfy.manymanycommands.commands.AbstractCommand
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class DeleteWarp : AbstractCommand() {
    override fun runImpl(context: CommandContext<ServerCommandSource>): Int {
        val warpName = getString(context, "warpName")
        val spe = context.source.player
        Configs.WARPS.updateIterable(WarpConfig::warps) {
            if (it.removeIf { warp -> warp.name == warpName }) spe?.sendMessage(Text.literal("Warp $warpName has been successfully removed").setStyle(Style.EMPTY.withColor(Formatting.GREEN)))
            else spe?.sendMessage(Text.literal("Warp $warpName can not be removed because it does not exist").setStyle(Style.EMPTY.withColor(Formatting.RED)))
        }
        return Command.SINGLE_SUCCESS
    }

}