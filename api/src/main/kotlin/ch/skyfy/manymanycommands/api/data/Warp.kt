package ch.skyfy.manymanycommands.api.data

import ch.skyfy.json5configlib.Validatable
import io.github.xn32.json5k.SerialComment
import kotlinx.serialization.Serializable

@Serializable
data class Warp(
    @SerialComment("The name of the warp")
    val name: String,
    @SerialComment("The location of the warp")
    val location: Location
) : Validatable