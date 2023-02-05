package ch.skyfy.manymanycommands.commands

import ch.skyfy.manymanycommands.api.CustomTeleportationStrategy
import ch.skyfy.manymanycommands.api.config.BackRule
import ch.skyfy.manymanycommands.api.data.Teleportation
import ch.skyfy.manymanycommands.api.utils.getBackRule
import ch.skyfy.manymanycommands.api.utils.getPlayerNameWithUUID
import ch.skyfy.manymanycommands.strategies.BackTeleportationStrategy
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity

class BackCmd : AbstractTeleportation<BackRule>(Teleportation.backTeleporting, Teleportation.backCooldowns) {

    companion object {
        fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
            val cmd = CommandManager.literal("back").requires { source -> source.hasPermissionLevel(0) }.executes(BackCmd())
            dispatcher.register(cmd)
        }
    }

    override fun runStrategy(context: CommandContext<ServerCommandSource>, spe: ServerPlayerEntity): CustomTeleportationStrategy<*>? {
        val rule = getBackRule(getPlayerNameWithUUID(spe)) ?: return null
        return BackTeleportationStrategy(rule)
    }
}