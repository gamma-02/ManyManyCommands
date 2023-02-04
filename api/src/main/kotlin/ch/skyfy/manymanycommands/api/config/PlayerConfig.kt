package ch.skyfy.manymanycommands.api.config

import ch.skyfy.json5configlib.Defaultable
import ch.skyfy.json5configlib.Validatable
import ch.skyfy.manymanycommands.api.data.Location
import io.github.xn32.json5k.SerialComment
import kotlinx.serialization.Serializable

@Serializable
data class PlayersConfig(
    @SerialComment("When player are joining the server for a first time, they will be added to a player group called DEFAULT")
    var shouldAutoAddPlayerToADefaultGroup: Boolean,
    @SerialComment("Will contain the list of players, with their created homes")
    var players: MutableSet<Player>,
    @SerialComment("The list of player group")
    var playerGroups: MutableList<PlayerGroup>
) : Validatable {
    override fun validateImpl(errors: MutableList<String>) {
        players.forEach { it.validateImpl(errors) }
    }
}

@Serializable
data class PlayerGroup(
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
    @SerialComment("The name of the warps groups where the players of this group can have access")
    val warpGroups: MutableList<String>,
    @SerialComment("The players that are member of this group")
    val players: MutableSet<String>
)

@Serializable
data class Player(
    var nameWithUUID: String,
    var homes: MutableSet<Home> = mutableSetOf()
) : Validatable {
    override fun validateImpl(errors: MutableList<String>) {
        homes.forEach { it.validateImpl(errors) }

        // TODO check in mojang database if this uuid is a real and premium minecraft account
    }
}

@Serializable
data class Home(
    var name: String,
    var location: Location
) : Validatable

class DefaultPlayerHomeConfig : Defaultable<PlayersConfig> {
    override fun getDefault(): PlayersConfig = PlayersConfig(
        true,
        mutableSetOf(),
        mutableListOf(
            PlayerGroup("DEFAULT", "SHORT", "SHORT", "SHORT", "DEFAULT", mutableListOf("DEFAULT"), mutableSetOf())
        )
    )
}