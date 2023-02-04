package ch.skyfy.manymanycommands

import ch.skyfy.json5configlib.updateIterable
import ch.skyfy.json5configlib.updateIterableNested
import ch.skyfy.manymanycommands.api.config.Configs
import ch.skyfy.manymanycommands.api.data.Group
import ch.skyfy.manymanycommands.api.data.Player
import ch.skyfy.manymanycommands.api.data.WarpGroup
import ch.skyfy.manymanycommands.api.persistent.HomesData
import ch.skyfy.manymanycommands.api.persistent.Persistent
import ch.skyfy.manymanycommands.api.utils.getPlayerNameWithUUID
import ch.skyfy.manymanycommands.commands.BackCmd
import ch.skyfy.manymanycommands.commands.ReloadFilesCmd
import ch.skyfy.manymanycommands.commands.WildCmd
import ch.skyfy.manymanycommands.commands.homes.HomesCmd
import ch.skyfy.manymanycommands.commands.warps.WarpsCmd
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Suppress("MemberVisibilityCanBePrivate", "unused")
class ManyManyCommandsMod : ModInitializer {

    companion object {
        const val MOD_ID: String = "manymanycommands"
        val LOGGER: Logger = LogManager.getLogger(ManyManyCommandsMod::class.java)
    }

    override fun onInitialize() {
        registerCommands()

        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            Persistent.HOMES.updateIterable(HomesData::players) {
                if (it.none { player -> player.nameWithUUID == getPlayerNameWithUUID(handler.player) })
                    it.add(Player(getPlayerNameWithUUID(handler.player)))
            }

            if (Configs.RULES.serializableData.shouldAutoAddPlayerToADefaultGroup) {

                // Add player to the default group
                Configs.RULES.serializableData.groups.firstOrNull { group -> group.name == "DEFAULT" }?.let { group ->
                    Configs.RULES.updateIterableNested(Group::players, group.players) { members ->
                        if(!members.contains(getPlayerNameWithUUID(handler.player)))members.add(getPlayerNameWithUUID(handler.player))
                    }
                }

                // Add player to the default warpGroup
                Configs.RULES.serializableData.warpGroups.firstOrNull { warpGroup -> warpGroup.name == "DEFAULT" }?.let {
                    Configs.RULES.updateIterableNested(WarpGroup::players, it.players) { members ->
                        if (!members.contains(getPlayerNameWithUUID(handler.player))) members.add(getPlayerNameWithUUID(handler.player))
                    }
                }
//                Configs.RULES.updateIterable(RulesConfig::groups) {
//                    it.find { playerGroup -> playerGroup.name == "DEFAULT" }?.players?.add(getPlayerNameWithUUID(handler.player))
//                }

            }
        }
    }

    private fun registerCommands() = CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
        HomesCmd().register(dispatcher)
        WarpsCmd.register(dispatcher)
        BackCmd.register(dispatcher)
        WildCmd.register(dispatcher)
        ReloadFilesCmd.register(dispatcher)

    }

}