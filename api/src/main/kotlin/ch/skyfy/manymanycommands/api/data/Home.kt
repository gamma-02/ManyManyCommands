package ch.skyfy.manymanycommands.api.data

import ch.skyfy.json5configlib.Validatable
import kotlinx.serialization.Serializable

@Serializable
data class Home(
    var name: String,
    var location: Location
) : Validatable