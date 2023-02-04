package ch.skyfy.manymanycommands.api.data

import ch.skyfy.json5configlib.Validatable
import kotlinx.serialization.Serializable

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