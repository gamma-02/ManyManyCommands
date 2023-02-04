package ch.skyfy.manymanycommands.api.data

import io.github.xn32.json5k.SerialComment
import kotlinx.serialization.Serializable

@Serializable
data class WarpGroup(
    @SerialComment("Name of the warp group")
    val name: String,
    @SerialComment("Name of the warp that are member of this group")
    val warps: MutableList<String>,
    @SerialComment("The players that are member of this group")
    val players: MutableSet<String>
)