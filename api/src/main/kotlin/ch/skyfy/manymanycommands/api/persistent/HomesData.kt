package ch.skyfy.manymanycommands.api.persistent

import ch.skyfy.json5configlib.Defaultable
import ch.skyfy.json5configlib.Validatable
import ch.skyfy.manymanycommands.api.data.Player
import io.github.xn32.json5k.SerialComment
import kotlinx.serialization.Serializable

@Serializable
data class HomesData(
    @SerialComment("Will contain the list of players, with the homes they own")
    var players: MutableSet<Player>,
) : Validatable {
    override fun validateImpl(errors: MutableList<String>) {
        players.forEach { it.validateImpl(errors) }
    }
}

class DefaultHomesData : Defaultable<HomesData> { override fun getDefault() = HomesData(mutableSetOf()) }