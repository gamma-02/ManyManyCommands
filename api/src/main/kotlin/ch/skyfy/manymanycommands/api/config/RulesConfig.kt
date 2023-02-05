package ch.skyfy.manymanycommands.api.config

import ch.skyfy.json5configlib.Defaultable
import ch.skyfy.json5configlib.Validatable
import ch.skyfy.manymanycommands.api.data.Group
import ch.skyfy.manymanycommands.api.data.WarpGroup
import io.github.xn32.json5k.SerialComment
import kotlinx.serialization.Serializable

@Serializable
data class RulesConfig(
    @SerialComment("If true, when players are joining the server for a first time, they will be added to a group called « DEFAULT » and a default warpGroup also called « DEFAULT »")
    var shouldAutoAddPlayerToADefaultGroup: Boolean,

    @SerialComment("If true, everytime you create a warp with /warp create <name>, it will be added to the warp group called « DEFAULT »")
    var shouldAddNewWarpToTheDefaultGroup: Boolean,

    @SerialComment("The list of group")
    var groups: MutableSet<Group>,

    @SerialComment("The list of warps groups that have been created")
    var warpGroups: MutableSet<WarpGroup>,

    @SerialComment("A list of homes rule. you can configure the home limit, the cooldown, etc.")
    val homesRules: MutableSet<HomesRules>,
    @SerialComment("Same but for the /back command")
    val backRules: MutableSet<BackRules>,
    @SerialComment("Same but for the /warps command")
    val warpRules: MutableSet<WarpRules>,
    @SerialComment("Same but for the /wild command")
    val wildRules: MutableSet<WildRules>,
    @SerialComment("Same but for the /tpaaccept command")
    val tpaAcceptRules: MutableSet<TpaAcceptRules>,

    @SerialComment("Some global rules about how the /wild command will work")
    val globalWildRule: GlobalWildRule
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
    @SerialComment("The number of seconds to remain standing without moving more than 2 blocks before the teleportation is effective")
    override val standStill: Int = 5,
    @SerialComment("The number of seconds you have to wait before teleporting a new time")
    override val cooldown: Int = 15,
    @SerialComment("The maximum number of homes")
    val maxHomes: Int = 3,
    @SerialComment("The list of dimension where you can use the /homes create <name> command")
    val allowedDimensionCreating: MutableSet<String>,
    @SerialComment("The list of dimension where you can use the /homes teleport command")
    val allowedDimensionTeleporting: MutableSet<String>
) : TeleportationRule(), Validatable {
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
    @SerialComment("The number of seconds to remain standing without moving more than 2 blocks before the teleportation is effective")
    override val standStill: Int = 5,
    @SerialComment("The number of seconds you have to wait before using /warps teleport a new time")
    override val cooldown: Int = 15,
    @SerialComment("The list of dimension where you can use the /warps teleport command")
    val allowedDimensionTeleporting: MutableSet<String>
) : TeleportationRule(), Validatable {
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
    @SerialComment("The number of seconds to remain standing without moving more than 2 blocks before the teleportation is effective")
    override val standStill: Int = 5,
    @SerialComment("The number of seconds you have to wait before using /back a new time")
    override val cooldown: Int = 15,
    @SerialComment("The list of dimension where you can use the /back command")
    val allowedDimension: MutableSet<String>
) : TeleportationRule(), Validatable {
    override fun validateImpl(errors: MutableList<String>) {
        if (cooldown < 0) errors.add("cooldown cannot have a negative value")
        if (standStill < 0) errors.add("standStill cannot have a negative value")
    }
}

@Serializable
data class WildRules(
    @SerialComment("The name to identify the rules")
    val name: String,
    @SerialComment("The rules to use in relation with /wild command")
    val wildRule: WildRule
) : Validatable

@Serializable
data class WildRule(
    @SerialComment("The number of seconds to remain standing without moving more than 2 blocks before the teleportation is effective")
    override val standStill: Int = 3,
    @SerialComment("The number of seconds you have to wait before using wild a new time")
    override val cooldown: Int = 15,
    @SerialComment("The number max of usage a player can use /wild")
    val maximumUsage: Int,
    @SerialComment("The list of dimension where you can use the /wild command")
    val allowedDimension: MutableSet<String>
) : TeleportationRule(), Validatable {
    override fun validateImpl(errors: MutableList<String>) {
        if (cooldown < 0) errors.add("cooldown cannot have a negative value")
        if (standStill < 0) errors.add("standStill cannot have a negative value")
    }
}

@Serializable
data class TpaAcceptRules(
    @SerialComment("The name to identify the rules")
    val name: String,
    @SerialComment("The rules to use in relation with /wild command")
    val tpaRule: TpaRule
) : Validatable

@Serializable
data class TpaRule(
    @SerialComment("The number of seconds to remain standing without moving more than 2 blocks before the teleportation is effective")
    override val standStill: Int = 3,
    @SerialComment("The number of seconds you have to wait before using wild a new time")
    override val cooldown: Int = 15,
    @SerialComment("The maximum of usage in total a player can be teleported using /tpa command set")
    val maximumUsageInTotal: Int,
    @SerialComment("The maximum number of usage per specified time a player can be teleported using /tpa command set. Default: From the first time the player uses the command, he can use it 2 times every 24 hours")
    val maximumUsagePerSpecificTime: UsagePerSpecificTime,
    @SerialComment("The list of dimension where you can use /tpa <name> or /tpahere <name>")
    val allowedDimension: MutableSet<String>
) : TeleportationRule(), Validatable {
    override fun validateImpl(errors: MutableList<String>) {
        if (cooldown < 0) errors.add("cooldown cannot have a negative value")
        if (standStill < 0) errors.add("standStill cannot have a negative value")
    }
}

@Serializable
data class UsagePerSpecificTime(
    @SerialComment("The time is seconds. Default -> 86400 (24 hours)")
    val time: Long,
    @SerialComment("How many times a player can be teleported using /tpa command set. Default -> 2 times")
    val maximumUsage: Int
) : Validatable

@Serializable
data class GlobalWildRule(
    @SerialComment("The area where player will get randomly teleported. By default, it's an area over the first 4000 blocks in each direction, but not more than 10000 blocks ")
    val range: Pair<Int, Int>,
) : Validatable {
    override fun validateImpl(errors: MutableList<String>) {
        if (range.first > range.second) errors.add("There is an error in file rules.json5 ! The minimum value is greater than the maximum value !")
        super.validateImpl(errors)
    }
}

sealed class TeleportationRule {
    abstract val standStill: Int
    abstract val cooldown: Int
}

class DefaultRulesConfig : Defaultable<RulesConfig> {
    override fun getDefault() = RulesConfig(
        shouldAutoAddPlayerToADefaultGroup = true,
        shouldAddNewWarpToTheDefaultGroup = true,
        groups = mutableSetOf(
            Group("DEFAULT", "SHORT", "SHORT", "SHORT", "DEFAULT", mutableSetOf())
        ),
        warpGroups = mutableSetOf(WarpGroup("DEFAULT", mutableListOf(), mutableSetOf())),
        homesRules = mutableSetOf(
            HomesRules("SHORT", HomesRule(3, 10, 3, mutableSetOf("minecraft:overworld"), mutableSetOf("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"))),
            HomesRules("MEDIUM", HomesRule(5, 30, 5, mutableSetOf("minecraft:overworld"), mutableSetOf("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"))),
            HomesRules("LONG", HomesRule(10, 60, 6, mutableSetOf("minecraft:overworld"), mutableSetOf("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"))),
            HomesRules("BORING", HomesRule(15, 120, 8, mutableSetOf("minecraft:overworld"), mutableSetOf("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"))),
        ),
        backRules = mutableSetOf(
            BackRules("SHORT", BackRule(3, 10, mutableSetOf("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"))),
            BackRules("MEDIUM", BackRule(5, 20, mutableSetOf("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end")))
        ),
        warpRules = mutableSetOf(
            WarpRules("SHORT", WarpRule(3, 10, mutableSetOf("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"))),
            WarpRules("MEDIUM", WarpRule(5, 30, mutableSetOf("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end")))
        ),
        wildRules = mutableSetOf(
            WildRules("DEFAULT", WildRule(5, 3600, 5, mutableSetOf("minecraft:overworld")))
        ),
        tpaAcceptRules = mutableSetOf(
            TpaAcceptRules("DEFAULT", TpaRule(3, 10, 1000, UsagePerSpecificTime(86400, 2), mutableSetOf("minecraft:overworld")))
        ),
        GlobalWildRule(Pair(4000, 10000))
    )

}