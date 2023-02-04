package ch.skyfy.manymanycommands.api.persistent

import ch.skyfy.json5configlib.ConfigData
import ch.skyfy.manymanycommands.api.ManyManyCommandsAPIMod

object Persistent {
    val HOMES = ConfigData.invoke<HomesData, DefaultHomesData>(ManyManyCommandsAPIMod.PERSISTENT_DIRECTORY.resolve("homes.json5"), true)
    val WARPS = ConfigData.invoke<WarpsData, DefaultWarpsData>(ManyManyCommandsAPIMod.PERSISTENT_DIRECTORY.resolve("warps.json5"), true)
    val OTHERS_DATA = ConfigData.invoke<OthersData, DefaultOthersData>(ManyManyCommandsAPIMod.PERSISTENT_DIRECTORY.resolve("others-data.json5"), true)
}