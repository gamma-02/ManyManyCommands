package ch.skyfy.manymanycommands.api.utils

import ch.skyfy.manymanycommands.api.ManyManyCommandsAPIMod
import ch.skyfy.manymanycommands.api.config.*
import ch.skyfy.manymanycommands.api.data.Player
import ch.skyfy.manymanycommands.api.data.Warp
import ch.skyfy.manymanycommands.api.persistent.Persistent
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

fun getPlayer(spe: ServerPlayerEntity): Player? = Persistent.HOMES.serializableData.players.firstOrNull { it.nameWithUUID == getPlayerNameWithUUID(spe) }

fun getHomesRule(playerNameWithUUID: String): HomesRule? {
    return Configs.RULES.serializableData.groups.firstOrNull { it.players.any { playerUUIDWithName -> playerUUIDWithName == playerNameWithUUID } }?.let { playerGroup ->
        Configs.RULES.serializableData.homesRules.firstOrNull { it.name == playerGroup.homesRulesName }?.homesRule
    }
}

fun getWarpRule(playerNameWithUUID: String): WarpRule? {
    return Configs.RULES.serializableData.groups.firstOrNull { it.players.any { playerUUIDWithName -> playerUUIDWithName == playerNameWithUUID } }?.let { playerGroup ->
        Configs.RULES.serializableData.warpRules.firstOrNull { it.name == playerGroup.warpRulesName }?.warpRule
    }
}

fun getBackRule(playerNameWithUUID: String): BackRule? {
    return Configs.RULES.serializableData.groups.firstOrNull { it.players.any { playerUUIDWithName -> playerUUIDWithName == playerNameWithUUID } }?.let { playerGroup ->
        Configs.RULES.serializableData.backRules.firstOrNull { it.name == playerGroup.backRulesName }?.backRule
    }
}

fun getWildRule(playerNameWithUUID: String): WildRule? {
    return Configs.RULES.serializableData.groups.firstOrNull { it.players.any { playerUUIDWithName -> playerUUIDWithName == playerNameWithUUID } }?.let { playerGroup ->
        Configs.RULES.serializableData.wildRules.firstOrNull { it.name == playerGroup.wildRulesName }?.wildRule
    }
}

fun getTpaAcceptRule(playerNameWithUUID: String): TpaRule? {
    return Configs.RULES.serializableData.groups.firstOrNull { it.players.any { playerUUIDWithName -> playerUUIDWithName == playerNameWithUUID } }?.let { playerGroup ->
        Configs.RULES.serializableData.tpaAcceptRules.firstOrNull { it.name == playerGroup.wildRulesName }?.tpaRule
    }
}

/**
 * Return all the warps that are accessible for the player
 */
fun getWarps(playerNameWithUUID: String): List<Warp> {
//    val playerGroups = Configs.RULES.serializableData.groups.filter { playerGroup ->
//        playerGroup.players.any { playerNameWithUUID2 -> playerNameWithUUID2 == playerNameWithUUID }
//    }

    return Persistent.WARPS.serializableData.warps.mapNotNull { warp ->
        if (Configs.RULES.serializableData.warpGroups.any { warpGroup ->
                warpGroup.players.any { playerNameWithUUID2 -> playerNameWithUUID2 == playerNameWithUUID }
            }) warp else null
    }
}

fun getPlayerNameWithUUID(spe: ServerPlayerEntity) = "${spe.name.string}#${spe.uuidAsString}"

fun isDistanceGreaterThan(startPos: Vec3d, nowPos: Vec3d, greaterThan: Int): Boolean {
    return (startPos.x.coerceAtLeast(nowPos.x) - startPos.x.coerceAtMost(nowPos.x) > greaterThan) ||
            (startPos.y.coerceAtLeast(nowPos.y) - startPos.y.coerceAtMost(nowPos.y) > greaterThan) ||
            (startPos.z.coerceAtLeast(nowPos.z) - startPos.z.coerceAtMost(nowPos.z) > greaterThan)
}