package ch.skyfy.manymanycommands.commands

import ch.skyfy.manymanycommands.api.config.Player
import ch.skyfy.manymanycommands.api.data.Location
import ch.skyfy.manymanycommands.api.data.Teleportation
import ch.skyfy.manymanycommands.api.persistent.Persistent
import ch.skyfy.manymanycommands.api.utils.getBackRule
import ch.skyfy.manymanycommands.api.utils.getPlayerNameWithUUID
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

    override fun getRule(player: Player): TeleportationRule? {
        val rule = getBackRule(player) ?: return null
        return TeleportationRule(rule.cooldown, rule.standStill)
    }

    override fun getLocation(context: CommandContext<ServerCommandSource>, spe: ServerPlayerEntity, player: Player): Location? {
        return Persistent.PERSISTENT_DATA.serializableData.previousLocation[getPlayerNameWithUUID(spe)]
    }

    override fun check(spe: ServerPlayerEntity, player: Player): Boolean {
        return true
    }

    override fun onTeleport(spe: ServerPlayerEntity) {}

}