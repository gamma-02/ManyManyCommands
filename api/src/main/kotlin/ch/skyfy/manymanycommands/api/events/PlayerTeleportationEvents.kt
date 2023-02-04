package ch.skyfy.manymanycommands.api.events

import ch.skyfy.manymanycommands.api.config.TeleportationRule
import ch.skyfy.manymanycommands.api.data.CommandType
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.server.network.ServerPlayerEntity


object PlayerTeleportationEvents {

//    inline val <reified RULE : TeleportationRule> KClass<RULE>.TELEPORTATION_DONE_MAP: MutableMap<KClass<RULE>, Event<TeleportationDone<RULE>>> get() = mutableMapOf()
//
//    inline fun <reified RULE : TeleportationRule> teleportationDoneEvent(): Event<TeleportationDone<RULE>> {
//        val event = EventFactory.createArrayBacked(TeleportationDone::class.java) { callbacks ->
//            TeleportationDone<RULE> { player, rule ->
//                for (callback in callbacks) callback.onTeleportationDone(player, rule)
//            }
//        }
//
//        if (!RULE::class.TELEPORTATION_DONE_MAP.containsKey(RULE::class)) {
//            RULE::class.TELEPORTATION_DONE_MAP[RULE::class] = event as Event<TeleportationDone<RULE>>
//        }
//        return RULE::class.TELEPORTATION_DONE_MAP[RULE::class] as Event<TeleportationDone<RULE>>
//    }

    val TELEPORTATION_DONE: Event<TeleportationDone> = EventFactory.createArrayBacked(TeleportationDone::class.java) { callbacks ->
        TeleportationDone { player, rule, commandType ->
            for (callback in callbacks) callback.onTeleportationDone(player, rule, commandType)
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

//inline fun <reified K : TeleportationRule, reified V> MutableMap<KClass<K>, Event<TeleportationDone<K>>>.putt(key: KClass<K>, value: Event<TeleportationDone<K>>?) {
//    this.put(key::class, value)
//}

fun interface TeleportationDone {

    fun onTeleportationDone(spe: ServerPlayerEntity, rule: TeleportationRule, commandType: CommandType)
}

fun interface TeleportationStandStillStarted {
    fun onTeleportationStandStill(spe: ServerPlayerEntity, rule: Any)
}

fun interface TeleportationCancelled {
    fun onTeleportationCancelled(spe: ServerPlayerEntity)
}