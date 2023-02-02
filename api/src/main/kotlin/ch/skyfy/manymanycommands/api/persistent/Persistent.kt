package ch.skyfy.manymanycommands.api.persistent

import ch.skyfy.json5configlib.ConfigData
import ch.skyfy.manymanycommands.api.ManyManyCommandsAPIMod

object Persistent {
    val PERSISTENT_DATA = ConfigData.invoke<PersistentData, DefaultPersistentData>(ManyManyCommandsAPIMod.PERSISTENT_DIRECTORY.resolve("data.json5"), true)
}