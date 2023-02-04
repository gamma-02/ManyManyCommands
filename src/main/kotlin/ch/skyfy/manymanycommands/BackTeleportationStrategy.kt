package ch.skyfy.manymanycommands

import ch.skyfy.manymanycommands.api.CustomTeleportationStrategy
import ch.skyfy.manymanycommands.api.config.BackRule
import ch.skyfy.manymanycommands.api.data.Location
import ch.skyfy.manymanycommands.api.data.Player
import ch.skyfy.manymanycommands.api.persistent.Persistent
import ch.skyfy.manymanycommands.api.utils.getPlayerNameWithUUID
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity

class BackTeleportationStrategy(override val rule: BackRule) : CustomTeleportationStrategy<BackRule>() {

    override fun getLocation(context: CommandContext<ServerCommandSource>, spe: ServerPlayerEntity, player: Player) = Persistent.OTHERS_DATA.serializableData.previousLocation[getPlayerNameWithUUID(spe)]

    override fun check(spe: ServerPlayerEntity, player: Player) = true

    override fun onTeleportDone(spe: ServerPlayerEntity, previousLocation: Location) {}
}