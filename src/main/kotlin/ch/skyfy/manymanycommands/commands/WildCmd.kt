package ch.skyfy.manymanycommands.commands

import ch.skyfy.json5configlib.update
import ch.skyfy.manymanycommands.ManyManyCommandsMod
import ch.skyfy.manymanycommands.api.CustomTeleportationStrategy
import ch.skyfy.manymanycommands.api.config.Configs
import ch.skyfy.manymanycommands.api.config.WildRule
import ch.skyfy.manymanycommands.api.data.Location
import ch.skyfy.manymanycommands.api.data.Teleportation
import ch.skyfy.manymanycommands.api.persistent.OthersData
import ch.skyfy.manymanycommands.api.persistent.Persistent
import ch.skyfy.manymanycommands.api.utils.getPlayerNameWithUUID
import ch.skyfy.manymanycommands.api.utils.getWildRule
import ch.skyfy.manymanycommands.strategies.WildTeleportationStrategy
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.silkmc.silk.core.task.infiniteMcCoroutineTask
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes

class WildCmd : AbstractTeleportation<WildRule>(Teleportation.wildTeleporting, Teleportation.wildCooldowns) {

    enum class Type { DIRECT, TIMED }

    companion object {
        fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
            val cmd = CommandManager.literal("wild").requires { source -> source.hasPermissionLevel(0) }.then(
                argument("type", StringArgumentType.string())
                    .suggests { _, builder -> CommandSource.suggestMatching(Type.values().map { it.name }, builder) }
                    .executes(WildCmd())
            )
            dispatcher.register(cmd)
        }

        init {
            infiniteMcCoroutineTask(sync = false, client = false, period = 10.minutes) {
                Persistent.OTHERS_DATA.update(OthersData::wildTimedLocation, getRandomLocation())
            }
        }

        fun getRandomLocation(): Location {
            val (min, max) = Configs.RULES.serializableData.globalWildRule.range
            var found = false
            var countIteration = 0
            val maxIteration = 20_000

            var x = 0.0
            var z = 0.0

            while (!found) {
                val min2 = if (Random.nextBoolean()) min else -min
                val max2 = if (Random.nextBoolean()) max else -max
                x = Random.nextInt(min2.coerceAtMost(max2), max2.coerceAtLeast(min2)).toDouble()
                z = Random.nextInt(min2.coerceAtMost(max2), max2.coerceAtLeast(min2)).toDouble()

                // check if the position is in the correct area
                if( (x > min || x < -min) && (z > min || z < -min) ) found = true

                if(countIteration++ >= maxIteration) {
                    ManyManyCommandsMod.LOGGER.warn("There were too many iterations to find a random teleportation point for the /wild command")
                    break
                }
            }

            return Location(x, 320.0, z, 0f, 0f, "minecraft:overworld")
        }
    }

    override fun runStrategy(context: CommandContext<ServerCommandSource>, spe: ServerPlayerEntity): CustomTeleportationStrategy<*>? {
        val rule = getWildRule(getPlayerNameWithUUID(spe)) ?: return null
        return WildTeleportationStrategy(rule)
    }

}