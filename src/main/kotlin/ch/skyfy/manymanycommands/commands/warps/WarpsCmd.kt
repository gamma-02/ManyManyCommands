package ch.skyfy.manymanycommands.commands.warps

import ch.skyfy.manymanycommands.api.config.Configs
import ch.skyfy.manymanycommands.api.utils.getPlayer
import ch.skyfy.manymanycommands.api.utils.getWarps
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.command.CommandSource.suggestMatching
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import java.util.concurrent.CompletableFuture


class WarpsCmd {

    companion object {
        fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
            val cmd = literal("warps").requires { source -> source.hasPermissionLevel(0) }
                .then(
                    literal("player").requires { source -> source.hasPermissionLevel(4) }
                        .then(
                            argument("playerName", StringArgumentType.string())
                                .suggests { context, suggestionBuilder -> EntityArgumentType.players().listSuggestions(context, suggestionBuilder) }
                                .then(
                                    literal("teleport")
                                        .then(argument("warpName", StringArgumentType.string()).suggests(::warps))
                                )
                        )
                )
                .then(
                    literal("create").requires { source -> source.hasPermissionLevel(4) }
                        .then(
                            argument("warpName", StringArgumentType.string()).executes(CreateWarp()).then(
                                argument("x", DoubleArgumentType.doubleArg()).then(
                                    argument("y", DoubleArgumentType.doubleArg()).then(
                                        argument("z", DoubleArgumentType.doubleArg()).then(
                                            argument("yaw", FloatArgumentType.floatArg()).then(
                                                argument("pitch", FloatArgumentType.floatArg()).executes(CreateWarpWithCoordinates())
                                            )
                                        )
                                    )
                                )
                            )
                        )
                ).then(
                    literal("delete").requires { source -> source.hasPermissionLevel(4) }.then(
                        argument("warpName", StringArgumentType.string()).suggests(::warps).executes(DeleteWarp())
                    )
                ).then(
                    literal("list").executes(ListWarp())
                ).then(
                    literal("teleport").then(
                        argument("warpName", StringArgumentType.string()).suggests(::warps).executes(TeleportWarp())
                    )
                )
            dispatcher.register(cmd)
        }

        private fun <S : ServerCommandSource> warps(commandContext: CommandContext<S>, suggestionsBuilder: SuggestionsBuilder): CompletableFuture<Suggestions> {
            if (commandContext.source.player !is ServerPlayerEntity) return suggestMatching(Configs.WARPS.serializableData.warps.map { it.name }, suggestionsBuilder)

            getPlayer(commandContext.source.player!!)?.let {player ->
                return suggestMatching(getWarps(player).map { it.name }, suggestionsBuilder)
            }

            return suggestMatching(listOf(), suggestionsBuilder)
        }
    }

}