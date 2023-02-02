package ch.skyfy.manymanycommands.api.data

import kotlinx.coroutines.CoroutineScope
import net.minecraft.util.math.Vec3d

object Teleportation {

     val homesTeleporting: MutableMap<String, Pair<CoroutineScope, Vec3d>> = mutableMapOf()
     val homesCooldowns: MutableMap<String, Long> = mutableMapOf()

     val warpsTeleporting: MutableMap<String, Pair<CoroutineScope, Vec3d>> = mutableMapOf()
     val warpsCooldowns: MutableMap<String, Long> = mutableMapOf()

     val backTeleporting: MutableMap<String, Pair<CoroutineScope, Vec3d>> = mutableMapOf()
     val backCooldowns: MutableMap<String, Long> = mutableMapOf()

}