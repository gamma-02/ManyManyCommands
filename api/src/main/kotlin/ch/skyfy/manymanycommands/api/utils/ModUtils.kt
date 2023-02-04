package ch.skyfy.manymanycommands.api.utils

import ch.skyfy.manymanycommands.api.ManyManyCommandsAPIMod
import ch.skyfy.manymanycommands.api.config.*
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d
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

fun getPlayer(spe: ServerPlayerEntity): Player? = Configs.PLAYERS.serializableData.players.firstOrNull { it.nameWithUUID == getPlayerNameWithUUID(spe) }

fun getHomesRule(player: Player): HomesRule? {
    return Configs.PLAYERS.serializableData.playerGroups.firstOrNull { it.players.any { playerUUIDWithName -> playerUUIDWithName == player.nameWithUUID } }?.let { playerGroup ->
        Configs.RULES.serializableData.homesRules.firstOrNull { it.name == playerGroup.homesRulesName }?.homesRule
    }
}

fun getWarpRule(player: Player): WarpRule? {
    return Configs.PLAYERS.serializableData.playerGroups.firstOrNull { it.players.any { playerUUIDWithName -> playerUUIDWithName == player.nameWithUUID } }?.let { playerGroup ->
        Configs.RULES.serializableData.warpRules.firstOrNull { it.name == playerGroup.warpRulesName }?.warpRule
    }
}

fun getBackRule(player: Player): BackRule? {
    return Configs.PLAYERS.serializableData.playerGroups.firstOrNull { it.players.any { playerUUIDWithName -> playerUUIDWithName == player.nameWithUUID } }?.let { playerGroup ->
        Configs.RULES.serializableData.backRules.firstOrNull { it.name == playerGroup.backRulesName }?.backRule
    }
}

fun getWildRule(player: Player): WildRule? {
    return Configs.PLAYERS.serializableData.playerGroups.firstOrNull { it.players.any { playerUUIDWithName -> playerUUIDWithName == player.nameWithUUID } }?.let { playerGroup ->
        Configs.RULES.serializableData.wildRules.firstOrNull { it.name == playerGroup.wildRulesName }?.wildRule
    }
}

/**
 * Return all the warps that are accessible for the player
 */
fun getWarps(player: Player): List<Warp> {
    val playerGroups = Configs.PLAYERS.serializableData.playerGroups.filter { playerGroup ->
        playerGroup.players.any { playerNameWithUUID -> playerNameWithUUID == player.nameWithUUID }
    }

    return Configs.WARPS.serializableData.warps.mapNotNull { warp ->
        if (Configs.WARPS.serializableData.groups.any { warpGroup ->
                playerGroups.any { playerGroup ->
                    playerGroup.warpGroups.any { it == warpGroup.name }
                } && warpGroup.warps.any { it == warp.name }
            }) warp else null
    }
}

//fun getPlayerUUIDWithName(player: Player): String {
//    return "${player.name}#${player.nameWithUUID}"
//}

fun getPlayerNameWithUUID(spe: ServerPlayerEntity): String {
    return "${spe.name.string}#${spe.uuidAsString}"
}

fun isDistanceGreaterThan(startPos: Vec3d, nowPos: Vec3d, greaterThan: Int): Boolean {
    return (startPos.x.coerceAtLeast(nowPos.x) - startPos.x.coerceAtMost(nowPos.x) > greaterThan) ||
            (startPos.y.coerceAtLeast(nowPos.y) - startPos.y.coerceAtMost(nowPos.y) > greaterThan) ||
            (startPos.z.coerceAtLeast(nowPos.z) - startPos.z.coerceAtMost(nowPos.z) > greaterThan)
}