package ch.skyfy.manymanycommands.api.config

import ch.skyfy.manymanycommands.api.ManyManyCommandsAPIMod
import ch.skyfy.json5configlib.ConfigData

object Configs {
    val PLAYERS = ConfigData.invoke<PlayersConfig, DefaultPlayerHomeConfig>(ManyManyCommandsAPIMod.CONFIG_DIRECTORY.resolve("players.json5"), true)
    val WARPS = ConfigData.invoke<WarpConfig, DefaultWarpConfig>(ManyManyCommandsAPIMod.CONFIG_DIRECTORY.resolve("warps.json5"), true)
    val RULES = ConfigData.invoke<RulesConfig, DefaultRulesConfig>(ManyManyCommandsAPIMod.CONFIG_DIRECTORY.resolve("rules.json5"), true)
}
