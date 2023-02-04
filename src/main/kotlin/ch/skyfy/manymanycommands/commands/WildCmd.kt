package ch.skyfy.manymanycommands.commands

import ch.skyfy.json5configlib.update
import ch.skyfy.manymanycommands.WildTeleportationStrategy
import ch.skyfy.manymanycommands.api.CustomTeleportationStrategy
import ch.skyfy.manymanycommands.api.config.WildRule
import ch.skyfy.manymanycommands.api.data.Location
import ch.skyfy.manymanycommands.api.data.Player
import ch.skyfy.manymanycommands.api.data.Teleportation
import ch.skyfy.manymanycommands.api.persistent.OthersData
import ch.skyfy.manymanycommands.api.persistent.Persistent
import ch.skyfy.manymanycommands.api.utils.getWildRule
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

    enum class Type {
        DIRECT,
        TIMED
    }

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
            val x = Random.nextInt(100, 5000).toDouble()
            val z = Random.nextInt(100, 5000).toDouble()
            return Location(x, 320.0, z, 0f, 0f, "minecraft:overworld")
        }
    }

    override fun runStrategy(context: CommandContext<ServerCommandSource>, spe: ServerPlayerEntity, player: Player): CustomTeleportationStrategy<*>? {
        val rule = getWildRule(player) ?: return null
        return WildTeleportationStrategy(rule)
    }

}