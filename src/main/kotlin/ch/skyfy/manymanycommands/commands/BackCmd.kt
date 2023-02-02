package ch.skyfy.manymanycommands.commands

import ch.skyfy.manymanycommands.api.config.Player
import ch.skyfy.manymanycommands.api.data.Location
import ch.skyfy.manymanycommands.api.data.Teleportation
import ch.skyfy.manymanycommands.api.persistent.Persistent
import ch.skyfy.manymanycommands.api.utils.getBackRule
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity

class BackCmd : AbstractTeleportation(Teleportation.backTeleporting, Teleportation.backCooldowns) {

    companion object {
        fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
            val cmd = CommandManager.literal("back").requires { source -> source.hasPermissionLevel(0) }.executes(BackCmd())
            dispatcher.register(cmd)
        }
    }

    override fun onTeleport(spe: ServerPlayerEntity) {}

    override fun getLocation(context: CommandContext<ServerCommandSource>, player: Player): Location? {
        return Persistent.PERSISTENT_DATA.serializableData.previousLocation[context.source.player?.uuidAsString]
    }

    override fun getRule(player: Player): TeleportationRule? {
        val rule = getBackRule(player) ?: return null
        return TeleportationRule(rule.cooldown, rule.standStill)
    }
}