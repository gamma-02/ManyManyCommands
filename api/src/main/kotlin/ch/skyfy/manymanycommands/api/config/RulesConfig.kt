package ch.skyfy.manymanycommands.api.config

import ch.skyfy.json5configlib.Defaultable
import ch.skyfy.json5configlib.Validatable
import io.github.xn32.json5k.SerialComment
import kotlinx.serialization.Serializable

@Serializable
data class RulesConfig(
    @SerialComment("A list of homes rule. you can configure the home limit, the cooldown, etc.")
    val homesRules: MutableList<HomesRules>,
    @SerialComment("Same but for the /back command")
    val backRules: MutableList<BackRules>,
    @SerialComment("Same here but for the /warps command")
    val warpRules: MutableList<WarpRules>
) : Validatable

@Serializable
data class HomesRules(
    @SerialComment("The name to identify the rules")
    val name: String,
    @SerialComment("The rules to use in relation with /homes commands")
    val homesRule: HomesRule
) : Validatable
@Serializable
data class HomesRule(
    @SerialComment("The maximum number of homes")
    val maxHomes: Int = 3,
    @SerialComment("The number of seconds you have to wait before teleporting a new time")
    val cooldown: Int = 15,
    @SerialComment("The number of seconds to remain standing without moving more than 2 blocks before the teleportation is effective")
    val standStill: Int = 5
) : Validatable {
    override fun validateImpl(errors: MutableList<String>) {
        if (maxHomes < 0) errors.add("maxHome cannot have a negative value")
        if (cooldown < 0) errors.add("cooldown cannot have a negative value")
        if (standStill < 0) errors.add("standStill cannot have a negative value")
    }
}

@Serializable
data class WarpRules(
    @SerialComment("The name to identify the rules")
    val name: String,
    @SerialComment("The rules to use in relation with /warps command")
    val warpRule: WarpRule
) : Validatable
@Serializable
data class WarpRule(
    @SerialComment("The number of seconds you have to wait before using /warps teleport a new time")
    val cooldown: Int = 15,
    @SerialComment("The number of seconds to remain standing without moving more than 2 blocks before the teleportation is effective")
    val standStill: Int = 5
) : Validatable {
    override fun validateImpl(errors: MutableList<String>) {
        if (cooldown < 0) errors.add("cooldown cannot have a negative value")
        if (standStill < 0) errors.add("standStill cannot have a negative value")
    }
}

@Serializable
data class BackRules(
    @SerialComment("The name to identify the rules")
    val name: String,
    @SerialComment("The rules to use in relation with /back command")
    val backRule: BackRule
) : Validatable
@Serializable
data class BackRule(
    @SerialComment("The number of seconds you have to wait before using /back a new time")
    val cooldown: Int = 15,
    @SerialComment("The number of seconds to remain standing without moving more than 2 blocks before the teleportation is effective")
    val standStill: Int = 5
) : Validatable {
    override fun validateImpl(errors: MutableList<String>) {
        if (cooldown < 0) errors.add("cooldown cannot have a negative value")
        if (standStill < 0) errors.add("standStill cannot have a negative value")
    }
}

class DefaultRulesConfig : Defaultable<RulesConfig> {
    override fun getDefault() = RulesConfig(
        mutableListOf(
            HomesRules("SHORT", HomesRule(3, 10, 3)),
            HomesRules("MEDIUM", HomesRule(4, 15, 5)),
            HomesRules("LONG", HomesRule(5, 30, 5)),
            HomesRules("BORING", HomesRule(6, 60, 5)),
        ),
        mutableListOf(
            BackRules("SHORT", BackRule(10, 3)),
            BackRules("MEDIUM", BackRule(20, 5))
        ),
        mutableListOf(
            WarpRules("SHORT", WarpRule(10, 3)),
            WarpRules("MEDIUM", WarpRule(20, 5))
        )
    )

}