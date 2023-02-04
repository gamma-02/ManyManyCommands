@file:Suppress("UNUSED_PARAMETER")

package ch.skyfy.manymanycommands.commands

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

abstract class AbstractTeleportation(
    private val teleporting: MutableMap<String, Pair<Job, Vec3d>>,
    private val cooldowns: MutableMap<String, Long>,
    override val coroutineContext: CoroutineContext = Dispatchers.Default
) : AbstractCommand(), CoroutineScope {

    init {
        EntityMoveCallback.EVENT.register(::onPlayerMove)
    }

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

    override fun runImpl(context: CommandContext<ServerCommandSource>): Int {
        if (context.source.player !is ServerPlayerEntity) return Command.SINGLE_SUCCESS

        val spe = context.source.player!!
        val player = Persistent.HOMES.serializableData.players.find { getPlayerNameWithUUID(spe) == it.nameWithUUID } ?: return Command.SINGLE_SUCCESS
        val rule = getRule(player) ?: return Command.SINGLE_SUCCESS

        // If any other teleportation are already in progress
        if (Teleportation.warpsTeleporting.containsKey(getPlayerNameWithUUID(spe))) {
            spe.sendMessage(Text.literal("A teleportation is already in progress").setStyle(Style.EMPTY.withColor(Formatting.RED)))
            return Command.SINGLE_SUCCESS
        }
        if (Teleportation.homesTeleporting.containsKey(getPlayerNameWithUUID(spe))) {
            spe.sendMessage(Text.literal("A teleportation is already in progress").setStyle(Style.EMPTY.withColor(Formatting.RED)))
            return Command.SINGLE_SUCCESS
        }
        if (Teleportation.backTeleporting.containsKey(getPlayerNameWithUUID(spe))) {
            spe.sendMessage(Text.literal("A teleportation is already in progress").setStyle(Style.EMPTY.withColor(Formatting.RED)))
            return Command.SINGLE_SUCCESS
        }
        if (Teleportation.wildTeleporting.containsKey(getPlayerNameWithUUID(spe))) {
            spe.sendMessage(Text.literal("A teleportation is already in progress").setStyle(Style.EMPTY.withColor(Formatting.RED)))
            return Command.SINGLE_SUCCESS
        }

        val loc = getLocation(context, spe, player) ?: return Command.SINGLE_SUCCESS

        if (!check(spe, player)) return Command.SINGLE_SUCCESS

        mcCoroutineScope.launch {
            val startTime = cooldowns[getPlayerNameWithUUID(spe)]

            if (startTime != null) {
                val elapsed = (System.currentTimeMillis() - startTime) / 1000L
                if (elapsed < rule.cooldown) {
                    spe.sendMessage(Text.literal("You must wait another ${rule.cooldown - elapsed} seconds before you can use this command again").setStyle(Style.EMPTY.withColor(Formatting.RED)))
                    return@launch
                }
            }


            val job = mcCoroutineTask(howOften = rule.standStill.toLong() + 1, period = 1.seconds){
                spe.sendMessage(Text.literal("${it.counterDownToOne - 1} seconds left before teleporting").setStyle(Style.EMPTY.withColor(Formatting.GOLD)), true)
            }

            teleporting.putIfAbsent(getPlayerNameWithUUID(spe), Pair(job, Vec3d(spe.pos.x, spe.pos.y, spe.pos.z)))

            job.invokeOnCompletion { throwable ->
                if(throwable != null)return@invokeOnCompletion
                spe.server.getWorld(spe.server.worldRegistryKeys.first { it.value.toString() == loc.dimension })?.let {
                    cooldowns[getPlayerNameWithUUID(spe)] = System.currentTimeMillis()
                    teleporting.remove(getPlayerNameWithUUID(spe))
                    onTeleport(spe)
                    spe.teleport(it, loc.x, loc.y, loc.z, loc.yaw, loc.pitch)
                }
            }
        }

//        launch {
//            val startTime = cooldowns[spe.uuidAsString]
//
//            if (startTime != null) {
//                val elapsed = (System.currentTimeMillis() - startTime) / 1000L
//                if (elapsed < rule.cooldown) {
//                    spe.sendMessage(Text.literal("You must wait another ${rule.cooldown - elapsed} seconds before you can use this command again").setStyle(Style.EMPTY.withColor(Formatting.RED)))
//                    return@launch
//                }
//            }
//
//            teleporting.putIfAbsent(spe.uuidAsString, Pair(this@launch, Vec3d(spe.pos.x, spe.pos.y, spe.pos.z)))
//
//            repeat(rule.standStill) { second ->
//                spe.sendMessage(Text.literal("${rule.standStill - second} seconds left before teleporting").setStyle(Style.EMPTY.withColor(Formatting.GOLD)), true)
//                delay(1000L)
//            }
//
//            spe.server.getWorld(spe.server.worldRegistryKeys.first { it.value.toString() == loc.dimension })?.let {
//                cooldowns[spe.uuidAsString] = System.currentTimeMillis()
//                teleporting.remove(spe.uuidAsString)
//                onTeleport(spe)
//                spe.teleport(it, loc.x, loc.y, loc.z, loc.yaw, loc.pitch)
//            }
//        }

        return Command.SINGLE_SUCCESS
    }

    abstract fun getRule(player: Player): TeleportationRule?

    abstract fun getLocation(context: CommandContext<ServerCommandSource>, spe: ServerPlayerEntity, player: Player): Location?

    abstract fun check(spe: ServerPlayerEntity, player: Player): Boolean

    abstract fun onTeleport(spe: ServerPlayerEntity)

    data class TeleportationRule(val cooldown: Int = 15, val standStill: Int = 5)
}