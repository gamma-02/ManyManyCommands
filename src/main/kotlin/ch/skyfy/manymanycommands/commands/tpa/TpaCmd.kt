package ch.skyfy.manymanycommands.commands.tpa

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.command.CommandSource.suggestMatching
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import java.util.concurrent.CompletableFuture
import java.util.stream.Collectors
import java.util.stream.Stream

class TpaCmd {

    enum class RequestType { REQUEST, REQUEST_HERE }

    companion object {

        val RECEIVED_REQUESTS: MutableMap<String, MutableSet<Pair<String, RequestType>>> = mutableMapOf()
//        val RECEIVED_HERE_REQUESTS: MutableMap<String, MutableSet<String>> = mutableMapOf()

        fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
            val cmd = CommandManager.literal("tpa").requires { source -> source.hasPermissionLevel(0) }.then(
                argument("playerName", StringArgumentType.string()).suggests(TpaCmd.Companion::onlinePlayers).executes(TpaRequest())
            )

            val tpahere = CommandManager.literal("tpahere").requires { source -> source.hasPermissionLevel(0) }.then(
                argument("playerName", StringArgumentType.string()).suggests(TpaCmd.Companion::onlinePlayers).executes(TpaHereRequest())
            )

            val tpaaccept = literal("tpaaccept").then(
                argument("playerName", StringArgumentType.string()).suggests(TpaCmd.Companion::playerWaitingForAReply).executes(TpaAccept())
            )

            val tpacancel = literal("tpacancel").then(
                argument("playerName", StringArgumentType.string()).suggests(TpaCmd.Companion::cancel).executes(TpaCancel())
            )

            dispatcher.register(cmd)
            dispatcher.register(tpahere)
            dispatcher.register(tpaaccept)
            dispatcher.register(tpacancel)
        }

        private fun <S : ServerCommandSource> cancel(commandContext: CommandContext<S>, suggestionsBuilder: SuggestionsBuilder): CompletableFuture<Suggestions> {
            if (commandContext.source.player !is ServerPlayerEntity) return suggestMatching(listOf(), suggestionsBuilder)
            val spe = commandContext.source.player!!

            val list = RECEIVED_REQUESTS.entries.mapNotNull { mutableEntry ->
                if (mutableEntry.value.any { it.first == spe.name.string }) mutableEntry.key else null
            }

            return suggestMatching(list, suggestionsBuilder)
        }

        private fun <S : ServerCommandSource> playerWaitingForAReply(commandContext: CommandContext<S>, suggestionsBuilder: SuggestionsBuilder): CompletableFuture<Suggestions> {
            if (commandContext.source.player !is ServerPlayerEntity) return suggestMatching(listOf(), suggestionsBuilder)
            return suggestMatching(RECEIVED_REQUESTS[commandContext.source.player!!.name.string]?.map { it.first } ?: listOf(), suggestionsBuilder)
        }

        private fun <S : ServerCommandSource> onlinePlayers(commandContext: CommandContext<S>, suggestionsBuilder: SuggestionsBuilder): CompletableFuture<Suggestions> {
            return suggestMatching(commandContext.source.server.playerManager.playerNames, suggestionsBuilder)
        }
    }
}