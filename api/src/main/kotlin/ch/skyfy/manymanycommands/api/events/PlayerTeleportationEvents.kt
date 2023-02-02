package ch.skyfy.manymanycommands.api.events

import ch.skyfy.manymanycommands.api.config.HomesRule
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.server.network.ServerPlayerEntity

object PlayerTeleportationEvents {

    val TELEPORTATION_DONE: Event<TeleportationDone> = EventFactory.createArrayBacked(TeleportationDone::class.java) { callbacks ->
        TeleportationDone { player, rule ->
            for (callback in callbacks) callback.onTeleportationDone(player, rule)
        }
    }

    val TELEPORTATION_STANDSTILL_STARTED: Event<TeleportationStandStillStarted> = EventFactory.createArrayBacked(TeleportationStandStillStarted::class.java) { callbacks ->
        TeleportationStandStillStarted { player, rule ->
            for (callback in callbacks) callback.onTeleportationStandStill(player, rule)
        }
    }

    val TELEPORTATION_CANCELLED: Event<TeleportationCancelled> = EventFactory.createArrayBacked(TeleportationCancelled::class.java) { callbacks ->
        TeleportationCancelled { player ->
            for (callback in callbacks) callback.onTeleportationCancelled(player)
        }
    }
}

fun interface TeleportationDone {
    fun onTeleportationDone(player: ServerPlayerEntity, homesRule: HomesRule)
}

fun interface TeleportationStandStillStarted {
    fun onTeleportationStandStill(player: ServerPlayerEntity, homesRule: HomesRule)
}

fun interface TeleportationCancelled {
    fun onTeleportationCancelled(player: ServerPlayerEntity)
}