package ch.skyfy.manymanycommands.api.config

import ch.skyfy.manymanycommands.api.ManyManyCommandsAPIMod
import ch.skyfy.json5configlib.ConfigData

object Configs {
    val RULES = ConfigData.invoke<RulesConfig, DefaultRulesConfig>(ManyManyCommandsAPIMod.CONFIG_DIRECTORY.resolve("rules.json5"), true)
}
