package ch.skyfy.manymanycommands

import ch.skyfy.json5configlib.updateIterable
import ch.skyfy.manymanycommands.api.config.Configs
import ch.skyfy.manymanycommands.api.config.Player
import ch.skyfy.manymanycommands.api.config.PlayersConfig
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
            Configs.PLAYERS.updateIterable(PlayersConfig::players) {
                if (it.none { player -> player.nameWithUUID == getPlayerNameWithUUID(handler.player) })
                    it.add(Player(getPlayerNameWithUUID(handler.player)))
            }

            if(Configs.PLAYERS.serializableData.shouldAutoAddPlayerToADefaultGroup) {
                Configs.PLAYERS.updateIterable(PlayersConfig::playerGroups) {
                    it.find { playerGroup -> playerGroup.name == "DEFAULT" }?.players?.add(getPlayerNameWithUUID(handler.player))
                }
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