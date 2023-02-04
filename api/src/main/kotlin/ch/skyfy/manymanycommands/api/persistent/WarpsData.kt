package ch.skyfy.manymanycommands.api.persistent

import ch.skyfy.json5configlib.Defaultable
import ch.skyfy.json5configlib.Validatable
import ch.skyfy.manymanycommands.api.data.Warp
import io.github.xn32.json5k.SerialComment
import kotlinx.serialization.Serializable

@Serializable
data class WarpsData(
    @SerialComment("The list of all warp that have been created")
    var warps: MutableSet<Warp>,
) : Validatable

class DefaultWarpsData : Defaultable<WarpsData> { override fun getDefault() = WarpsData(mutableSetOf()) }