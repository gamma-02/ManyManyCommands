package ch.skyfy.manymanycommands.api.utils

import ch.skyfy.manymanycommands.api.ManyManyCommandsAPIMod
import ch.skyfy.manymanycommands.api.config.*
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.command.CommandSource
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d
import java.util.concurrent.CompletableFuture
import kotlin.io.path.createDirectory
import kotlin.io.path.exists

fun setupConfigDirectory() {
    try {
        if (!ManyManyCommandsAPIMod.CONFIG_DIRECTORY.exists()) ManyManyCommandsAPIMod.CONFIG_DIRECTORY.createDirectory()
    } catch (e: java.lang.Exception) {
        ManyManyCommandsAPIMod.LOGGER.fatal("An exception occurred. Could not create the root folder that should contain the configuration files")
        throw RuntimeException(e)
    }
}

fun setupExtensionDirectory() {
    try {
        if (!ManyManyCommandsAPIMod.EXTENSION_DIRECTORY.exists()) ManyManyCommandsAPIMod.EXTENSION_DIRECTORY.createDirectory()
    } catch (e: java.lang.Exception) {
        ManyManyCommandsAPIMod.LOGGER.fatal("An exception occurred. Could not create the extension folder that should contain the extensions files")
        throw RuntimeException(e)
    }
}

fun <S : ServerCommandSource> getConfigFiles(commandContext: CommandContext<S>, suggestionsBuilder: SuggestionsBuilder): CompletableFuture<Suggestions> {
    val list = mutableListOf<String>()
    list.add(Configs.PLAYERS_CONFIG.relativePath.fileName.toString())
    list.add(Configs.RULES_CONFIG.relativePath.fileName.toString())
    list.add("ALL")
    return CommandSource.suggestMatching(list, suggestionsBuilder)
}

fun getPlayer(playerEntity: ServerPlayerEntity): Player? = Configs.PLAYERS_CONFIG.serializableData.players.firstOrNull { it.uuid == playerEntity.uuidAsString }

fun getHomesRule(player: Player): HomesRule? {
    return Configs.PLAYERS_CONFIG.serializableData.playerGroups.firstOrNull { it.players.any { p -> p.uuid == player.uuid } }?.let { playerGroup ->
        Configs.RULES_CONFIG.serializableData.homesRules.firstOrNull { it.name == playerGroup.homesRulesName }?.homesRule
    }
}

fun getWarpRule(player: Player): WarpRule? {
    return Configs.PLAYERS_CONFIG.serializableData.playerGroups.firstOrNull { it.players.any { p -> p.uuid == player.uuid } }?.let { playerGroup ->
        Configs.RULES_CONFIG.serializableData.warpRules.firstOrNull { it.name == playerGroup.warpRulesName }?.warpRule
    }
}

fun getBackRule(player: Player): BackRule? {
    return Configs.PLAYERS_CONFIG.serializableData.playerGroups.firstOrNull { it.players.any { p -> p.uuid == player.uuid } }?.let { playerGroup ->
        Configs.RULES_CONFIG.serializableData.backRules.firstOrNull { it.name == playerGroup.backRulesName }?.backRule
    }
}

fun getWarps(player: Player): List<Warp> {
    val playerGroups = Configs.PLAYERS_CONFIG.serializableData.playerGroups.filter { playerGroup ->
        playerGroup.players.any { it.uuid == player.uuid }
    }

    val w = Configs.WARPS.serializableData.warps.map { warp ->
        if(Configs.WARPS.serializableData.groups.any { warpGroup ->
            playerGroups.any { playerGroup ->
                playerGroup.warpGroups.any { it == warpGroup.name }
            } && warpGroup.warps.any { it == warp.name }
        }) warp else null
    }.filterNotNull()
    return w
}

fun isDistanceGreaterThan(startPos: Vec3d, nowPos: Vec3d, greaterThan: Int): Boolean {
    return (startPos.x.coerceAtLeast(nowPos.x) - startPos.x.coerceAtMost(nowPos.x) > greaterThan) ||
            (startPos.y.coerceAtLeast(nowPos.y) - startPos.y.coerceAtMost(nowPos.y) > greaterThan) ||
            (startPos.z.coerceAtLeast(nowPos.z) - startPos.z.coerceAtMost(nowPos.z) > greaterThan)
}