@file:Suppress("UNUSED_PARAMETER")

package ch.skyfy.manymanycommands.commands

import ch.skyfy.manymanycommands.api.CustomTeleportationStrategy
import ch.skyfy.manymanycommands.api.config.TeleportationRule
import ch.skyfy.manymanycommands.api.data.Location
import ch.skyfy.manymanycommands.api.data.Player
import ch.skyfy.manymanycommands.api.data.Teleportation
import ch.skyfy.manymanycommands.api.events.PlayerTeleportationEvents
import ch.skyfy.manymanycommands.api.persistent.Persistent
import ch.skyfy.manymanycommands.api.utils.getPlayerNameWithUUID
import ch.skyfy.manymanycommands.api.utils.isDistanceGreaterThan
import ch.skyfy.manymanycommands.callbacks.EntityMoveCallback
import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import kotlinx.coroutines.*
import net.minecraft.entity.Entity
import net.minecraft.entity.MovementType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.math.Vec3d
import net.silkmc.silk.core.task.mcCoroutineScope
import net.silkmc.silk.core.task.mcCoroutineTask
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

abstract class AbstractTeleportation<R : TeleportationRule>(
    private val teleporting: MutableMap<String, Pair<Job, Vec3d>>,
    private val cooldowns: MutableMap<String, Long>,
    override val coroutineContext: CoroutineContext = Dispatchers.Default
) : AbstractCommand(), CoroutineScope {

    init { EntityMoveCallback.EVENT.register(::onPlayerMove) }

    private fun onPlayerMove(entity: Entity, movementType: MovementType, movement: Vec3d): ActionResult {
        if (entity is ServerPlayerEntity) {
            if (teleporting.containsKey(getPlayerNameWithUUID(entity))) {
                val value = teleporting[getPlayerNameWithUUID(entity)]!!
                if (isDistanceGreaterThan(value.second, entity.pos, 2)) {
                    value.first.cancel()
                    teleporting.remove(getPlayerNameWithUUID(entity))
                    entity.sendMessage(Text.literal("You moved ! teleportation cancelled !").setStyle(Style.EMPTY.withColor(Formatting.RED)))
                    PlayerTeleportationEvents.TELEPORTATION_CANCELLED.invoker().onTeleportationCancelled(entity)
                    return ActionResult.PASS
                }
            }
        }
        return ActionResult.PASS
    }

    abstract fun runStrategy(context: CommandContext<ServerCommandSource>, spe: ServerPlayerEntity, player: Player) : CustomTeleportationStrategy<*>?

    override fun runImpl(context: CommandContext<ServerCommandSource>): Int {
        return impl(context)
    }

    private fun impl(context: CommandContext<ServerCommandSource>) : Int {
        if (context.source.player !is ServerPlayerEntity) return Command.SINGLE_SUCCESS

        val spe = context.source.player!!
        val player = Persistent.HOMES.serializableData.players.find { getPlayerNameWithUUID(spe) == it.nameWithUUID } ?: return Command.SINGLE_SUCCESS
        val strategy = runStrategy(context, spe, player) ?: return Command.SINGLE_SUCCESS

        // If any other teleportation are already in progress
        if (Teleportation.warpsTeleporting.containsKey(player.nameWithUUID)) {
            spe.sendMessage(Text.literal("A teleportation is already in progress").setStyle(Style.EMPTY.withColor(Formatting.RED)))
            return Command.SINGLE_SUCCESS
        }
        if (Teleportation.homesTeleporting.containsKey(player.nameWithUUID)) {
            spe.sendMessage(Text.literal("A teleportation is already in progress").setStyle(Style.EMPTY.withColor(Formatting.RED)))
            return Command.SINGLE_SUCCESS
        }
        if (Teleportation.backTeleporting.containsKey(player.nameWithUUID)) {
            spe.sendMessage(Text.literal("A teleportation is already in progress").setStyle(Style.EMPTY.withColor(Formatting.RED)))
            return Command.SINGLE_SUCCESS
        }
        if (Teleportation.wildTeleporting.containsKey(player.nameWithUUID)) {
            spe.sendMessage(Text.literal("A teleportation is already in progress").setStyle(Style.EMPTY.withColor(Formatting.RED)))
            return Command.SINGLE_SUCCESS
        }

        val loc = strategy.getLocation(context, spe, player) ?: return Command.SINGLE_SUCCESS

        if (!strategy.check(spe, player)) return Command.SINGLE_SUCCESS

        mcCoroutineScope.launch {
            val startTime = cooldowns[player.nameWithUUID]

            if (startTime != null) {
                val elapsed = (System.currentTimeMillis() - startTime) / 1000L
                if (elapsed < strategy.rule.cooldown) {
                    spe.sendMessage(Text.literal("You must wait another ${strategy.rule.cooldown - elapsed} seconds before you can use this command again").setStyle(Style.EMPTY.withColor(Formatting.RED)))
                    return@launch
                }
            }

            val job = mcCoroutineTask(howOften = strategy.rule.standStill.toLong() + 1, period = 1.seconds) {
                spe.sendMessage(Text.literal("${it.counterDownToOne - 1} seconds left before teleporting").setStyle(Style.EMPTY.withColor(Formatting.GOLD)), true)
            }

            teleporting.putIfAbsent(player.nameWithUUID, Pair(job, Vec3d(spe.pos.x, spe.pos.y, spe.pos.z)))

            job.invokeOnCompletion { throwable ->
                if (throwable != null) return@invokeOnCompletion
                spe.server.getWorld(spe.server.worldRegistryKeys.first { it.value.toString() == loc.dimension })?.let {
                    cooldowns[player.nameWithUUID] = System.currentTimeMillis()
                    teleporting.remove(player.nameWithUUID)
                    val previousLocation = Location(spe.x, spe.y, spe.z, spe.yaw, spe.pitch, spe.world.dimensionKey.value.toString())
                    spe.teleport(it, loc.x, loc.y, loc.z, loc.yaw, loc.pitch)
                    strategy.onTeleportDone(spe,previousLocation)
                }
            }
        }
        return Command.SINGLE_SUCCESS
    }
}