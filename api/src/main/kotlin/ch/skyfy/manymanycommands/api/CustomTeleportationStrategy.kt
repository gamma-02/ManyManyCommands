package ch.skyfy.manymanycommands.api

import ch.skyfy.manymanycommands.api.config.TeleportationRule
import ch.skyfy.manymanycommands.api.data.Location
import ch.skyfy.manymanycommands.api.data.Player
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity

abstract class CustomTeleportationStrategy<RULE : TeleportationRule> {

    abstract val rule: RULE

    open fun getPlayerToTeleport(context: CommandContext<ServerCommandSource>, spe: ServerPlayerEntity) : ServerPlayerEntity = spe

    abstract fun getLocation(context: CommandContext<ServerCommandSource>, spe: ServerPlayerEntity): Location?

    abstract fun check(spe: ServerPlayerEntity): Boolean

    abstract fun onTeleportDone(spe: ServerPlayerEntity, previousLocation: Location)
}