package ch.skyfy.manymanycommands.commands

import ch.skyfy.json5configlib.update
import ch.skyfy.json5configlib.updateMap
import ch.skyfy.manymanycommands.api.config.WildRule
import ch.skyfy.manymanycommands.api.data.Location
import ch.skyfy.manymanycommands.api.data.Player
import ch.skyfy.manymanycommands.api.data.Teleportation
import ch.skyfy.manymanycommands.api.persistent.OthersData
import ch.skyfy.manymanycommands.api.persistent.Persistent
import ch.skyfy.manymanycommands.api.utils.getPlayerNameWithUUID
import ch.skyfy.manymanycommands.api.utils.getWildRule
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.silkmc.silk.core.task.infiniteMcCoroutineTask
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes

class WildCmd : AbstractTeleportation(Teleportation.wildTeleporting, Teleportation.wildCooldowns) {

    enum class Type {
        DIRECT,
        TIMED
    }

    private var wildRule: WildRule? = null

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

    override fun getRule(player: Player): TeleportationRule? {
        val rule = getWildRule(player) ?: return null
        this.wildRule = rule
        return TeleportationRule(rule.cooldown, rule.standStill)
    }

    override fun getLocation(context: CommandContext<ServerCommandSource>, spe: ServerPlayerEntity, player: Player): Location? {
        wildRule?.let { wildRule ->
            if (wildRule.allowedDimension.none { allowedDim -> allowedDim == spe.world.dimensionKey.value.toString() }) {
                spe.sendMessage(Text.literal("You cannot use this command in this dimension").setStyle(Style.EMPTY.withColor(Formatting.RED)))
                return null
            }
        }

        return when (Type.valueOf(getString(context, "type"))) {
            Type.DIRECT -> getRandomLocation()
            Type.TIMED -> Persistent.OTHERS_DATA.serializableData.wildTimedLocation
        }
    }

    override fun check(spe: ServerPlayerEntity, player: Player): Boolean {
        Persistent.OTHERS_DATA.serializableData.currentUsageOfWildCommand[player.nameWithUUID]?.let { currentUsageOfWildCommand ->
            if (wildRule != null && currentUsageOfWildCommand > wildRule!!.maximumUsage) {
                spe.sendMessage(Text.literal("You have reached the maximum number of uses for this command").setStyle(Style.EMPTY.withColor(Formatting.RED)))
                return false
            }
        }
        return true
    }

    override fun onTeleport(spe: ServerPlayerEntity) {
        spe.addStatusEffect(StatusEffectInstance(StatusEffects.RESISTANCE, 20 * 20, 9))
        spe.addStatusEffect(StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 20 * 20, 9))
        spe.sendMessage(Text.literal("You have been teleported to an unknown lands, Good Adventure ! ").setStyle(Style.EMPTY.withColor(Formatting.GREEN)))
        Persistent.OTHERS_DATA.updateMap(OthersData::currentUsageOfWildCommand) { currentUsageOfWildCommand ->
            currentUsageOfWildCommand.merge(getPlayerNameWithUUID(spe), 1, Int::plus)
        }
    }


}