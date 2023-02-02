package ch.skyfy.manymanycommands.api.config

import ch.skyfy.json5configlib.Defaultable
import ch.skyfy.json5configlib.Validatable
import ch.skyfy.manymanycommands.api.data.Location
import io.github.xn32.json5k.SerialComment
import kotlinx.serialization.Serializable

@Serializable
data class WarpConfig(
    @SerialComment("If true, everytime you create a warp with /warp create <name>, it will be added to the warp group called <<DEFAULT>>")
    var shouldAddNewWarpToTheDefaultGroup: Boolean,
    @SerialComment("The list of all warp that have been created")
    var warps: MutableSet<Warp>,
    @SerialComment("The list of all warp groups that have been created")
    var groups: MutableList<WarpGroup>
) : Validatable

@Serializable
data class Warp(
    @SerialComment("The name of the warp")
    val name: String,
    @SerialComment("The location of the warp")
    val location: Location
) : Validatable

@Serializable
data class WarpGroup(
    @SerialComment("Name of the warp group")
    val name: String,
    @SerialComment("Name of the warp that are member of this group")
    val warps: MutableList<String>
)

class DefaultWarpConfig : Defaultable<WarpConfig> {
    override fun getDefault(): WarpConfig {
        return WarpConfig(
            true,
            mutableSetOf(),
            mutableListOf(WarpGroup("DEFAULT", mutableListOf()))
        )
    }
}