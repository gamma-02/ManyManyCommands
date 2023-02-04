package ch.skyfy.manymanycommands.api

import ch.skyfy.manymanycommands.api.config.TeleportationRule
import ch.skyfy.manymanycommands.api.data.Location
import ch.skyfy.manymanycommands.api.data.Player
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity

abstract class CustomTeleportationStrategy<RULE : TeleportationRule> {

    abstract val rule: RULE

    abstract fun getLocation(context: CommandContext<ServerCommandSource>, spe: ServerPlayerEntity, player: Player): Location?

    abstract fun check(spe: ServerPlayerEntity, player: Player): Boolean

    abstract fun onTeleportDone(spe: ServerPlayerEntity, previousLocation: Location)
}