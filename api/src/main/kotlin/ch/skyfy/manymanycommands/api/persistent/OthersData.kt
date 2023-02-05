package ch.skyfy.manymanycommands.api.persistent

import ch.skyfy.json5configlib.Defaultable
import ch.skyfy.json5configlib.Validatable
import ch.skyfy.manymanycommands.api.data.Location
import kotlinx.serialization.Serializable

@Serializable
data class OthersData(
    val previousLocation: MutableMap<String, Location>,
    val currentUsageOfWildCommand: MutableMap<String, Int>,
    var wildTimedLocation: Location?,
    var tpaAcceptUsagePerTime: MutableMap<String, Pair<Long, Int>>
) : Validatable

class DefaultOthersData : Defaultable<OthersData> { override fun getDefault() = OthersData(mutableMapOf(), mutableMapOf(), null, mutableMapOf()) }