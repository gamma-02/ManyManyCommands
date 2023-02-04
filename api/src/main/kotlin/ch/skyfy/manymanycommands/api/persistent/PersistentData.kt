package ch.skyfy.manymanycommands.api.persistent

import ch.skyfy.json5configlib.Defaultable
import ch.skyfy.json5configlib.Validatable
import ch.skyfy.manymanycommands.api.data.Location
import kotlinx.serialization.Serializable

@Serializable
data class PersistentData(
    val previousLocation: MutableMap<String, Location>,
    val currentUsageOfWildCommand: MutableMap<String, Int>,
    var wildTimedLocation: Location?
) : Validatable

class DefaultPersistentData : Defaultable<PersistentData> {
    override fun getDefault(): PersistentData {
        return PersistentData(mutableMapOf(), mutableMapOf(), null)
    }
}