package ch.skyfy.manymanycommands.api.data

import ch.skyfy.json5configlib.Validatable
import io.github.xn32.json5k.SerialComment
import kotlinx.serialization.Serializable

@Serializable
data class Group(
    @SerialComment("The name of the group")
    val name: String,
    @SerialComment("The name of the homesRules to use")
    val homesRulesName: String,
    @SerialComment("The name of the warpRules to use")
    val warpRulesName: String,
    @SerialComment("The name of the backRules to use")
    val backRulesName: String,
    @SerialComment("The name of the wildRules to use")
    val wildRulesName: String,
//    @SerialComment("The name of the warps groups where the players of this group can have access")
//    val warpGroups: MutableList<String>,
    @SerialComment("The players that are member of this group")
    val players: MutableSet<String>
) : Validatable