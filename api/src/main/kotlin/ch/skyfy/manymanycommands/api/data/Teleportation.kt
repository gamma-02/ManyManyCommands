package ch.skyfy.manymanycommands.api.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import net.minecraft.util.math.Vec3d

object Teleportation {

     val homesTeleporting: MutableMap<String, Pair<Job, Vec3d>> = mutableMapOf()
     val homesCooldowns: MutableMap<String, Long> = mutableMapOf()

     val warpsTeleporting: MutableMap<String, Pair<Job, Vec3d>> = mutableMapOf()
     val warpsCooldowns: MutableMap<String, Long> = mutableMapOf()

     val backTeleporting: MutableMap<String, Pair<Job, Vec3d>> = mutableMapOf()
     val backCooldowns: MutableMap<String, Long> = mutableMapOf()

     val wildTeleporting: MutableMap<String, Pair<Job, Vec3d>> = mutableMapOf()
     val wildCooldowns: MutableMap<String, Long> = mutableMapOf()

}