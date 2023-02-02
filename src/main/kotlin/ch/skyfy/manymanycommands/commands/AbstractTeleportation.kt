@file:Suppress("UNUSED_PARAMETER")

package ch.skyfy.manymanycommands.commands

import ch.skyfy.manymanycommands.api.config.Configs
import ch.skyfy.manymanycommands.api.config.Player
import ch.skyfy.manymanycommands.api.data.Location
import ch.skyfy.manymanycommands.api.data.Teleportation
import ch.skyfy.manymanycommands.api.events.PlayerTeleportationEvents
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
import kotlin.coroutines.CoroutineContext

abstract class AbstractTeleportation(
    private val teleporting: MutableMap<String, Pair<CoroutineScope, Vec3d>>,
    private val cooldowns: MutableMap<String, Long>,
    override val coroutineContext: CoroutineContext = Dispatchers.Default
) : AbstractCommand(), CoroutineScope {

    init {
        EntityMoveCallback.EVENT.register(::onPlayerMove)
    }

    private fun onPlayerMove(entity: Entity, movementType: MovementType, movement: Vec3d): ActionResult {
        if (entity is ServerPlayerEntity) {
            if (teleporting.containsKey(entity.uuidAsString)) {
                val value = teleporting[entity.uuidAsString]!!
                if (isDistanceGreaterThan(value.second, entity.pos, 2)) {
                    value.first.cancel()
                    teleporting.remove(entity.uuidAsString)
                    entity.sendMessage(Text.literal("You moved ! teleportation cancelled !").setStyle(Style.EMPTY.withColor(Formatting.RED)))
                    PlayerTeleportationEvents.TELEPORTATION_CANCELLED.invoker().onTeleportationCancelled(entity)
                    return ActionResult.PASS
                }
            }
        }
        return ActionResult.PASS
    }

    override fun runImpl(context: CommandContext<ServerCommandSource>): Int {
        if (context.source.player !is ServerPlayerEntity) return 1

        val spe = context.source.player!!
        val player = Configs.PLAYERS_CONFIG.serializableData.players.find { spe.uuidAsString == it.uuid } ?: return Command.SINGLE_SUCCESS
        val rule = getRule(player) ?: return Command.SINGLE_SUCCESS

        // If any other teleportation are already in progress
        if (Teleportation.warpsTeleporting.containsKey(spe.uuidAsString)) {
            spe.sendMessage(Text.literal("A teleportation is already in progress").setStyle(Style.EMPTY.withColor(Formatting.RED)))
            return Command.SINGLE_SUCCESS
        }
        if (Teleportation.backTeleporting.containsKey(spe.uuidAsString)) {
            spe.sendMessage(Text.literal("A teleportation is already in progress").setStyle(Style.EMPTY.withColor(Formatting.RED)))
            return Command.SINGLE_SUCCESS
        }
        if (Teleportation.homesTeleporting.containsKey(spe.uuidAsString)) {
            spe.sendMessage(Text.literal("A teleportation is already in progress").setStyle(Style.EMPTY.withColor(Formatting.RED)))
            return Command.SINGLE_SUCCESS
        }

        val loc = getLocation(context, player) ?: return Command.SINGLE_SUCCESS

        launch {
            val startTime = cooldowns[spe.uuidAsString]

            if (startTime != null) {
                val elapsed = (System.currentTimeMillis() - startTime) / 1000L
                if (elapsed < rule.cooldown) {
                    spe.sendMessage(Text.literal("You must wait another ${rule.cooldown - elapsed} seconds before you can use this command again").setStyle(Style.EMPTY.withColor(Formatting.RED)))
                    return@launch
                }
            }

            teleporting.putIfAbsent(spe.uuidAsString, Pair(this@launch, Vec3d(spe.pos.x, spe.pos.y, spe.pos.z)))

            repeat(rule.standStill) { second ->
                spe.sendMessage(Text.literal("${rule.standStill - second} seconds left before teleporting").setStyle(Style.EMPTY.withColor(Formatting.GOLD)), true)
                delay(1000L)
            }

            spe.server.getWorld(spe.server.worldRegistryKeys.first { it.value.toString() == loc.dimension })?.let {
                cooldowns[spe.uuidAsString] = System.currentTimeMillis()
                teleporting.remove(spe.uuidAsString)
                onTeleport(spe)
                spe.teleport(it, loc.x, loc.y, loc.z, loc.yaw, loc.pitch)
            }
        }

        return 1
    }

    abstract fun getRule(player: Player) :  TeleportationRule?

    abstract fun getLocation(context: CommandContext<ServerCommandSource>, player: Player) : Location?

    abstract fun onTeleport(spe: ServerPlayerEntity)

    data class TeleportationRule(val cooldown: Int = 15, val standStill: Int = 5)
}