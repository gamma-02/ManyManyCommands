package ch.skyfy.manymanycommands.api.data

import ch.skyfy.json5configlib.Validatable
import kotlinx.serialization.Serializable

@Serializable
data class Location(val x: Double, val y: Double, val z: Double, val pitch: Float, val yaw: Float, val dimension: String) : Validatable