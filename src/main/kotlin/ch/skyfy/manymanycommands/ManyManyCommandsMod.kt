package ch.skyfy.manymanycommands

import ch.skyfy.json5configlib.updateIterable
import ch.skyfy.manymanycommands.api.config.Configs
import ch.skyfy.manymanycommands.api.config.Player
import ch.skyfy.manymanycommands.api.config.PlayersConfig
import ch.skyfy.manymanycommands.api.config.SimplePlayer
import ch.skyfy.manymanycommands.commands.BackCmd
import ch.skyfy.manymanycommands.commands.ReloadFilesCmd
import ch.skyfy.manymanycommands.commands.homes.HomesCmd
import ch.skyfy.manymanycommands.commands.warps.WarpsCmd
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Suppress("MemberVisibilityCanBePrivate")
class ManyManyCommandsMod : ModInitializer {

    companion object {
        const val MOD_ID: String = "manymanycommands"
        val LOGGER: Logger = LogManager.getLogger(ManyManyCommandsMod::class.java)
    }

    override fun onInitialize() {
        registerCommands()

        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            Configs.PLAYERS_CONFIG.updateIterable(PlayersConfig::players) {
                if (it.none { player -> player.uuid == handler.player.uuidAsString })
                    it.add(Player(uuid = handler.player.uuidAsString, handler.player.name.string))
            }

            if(Configs.PLAYERS_CONFIG.serializableData.shouldAutoAddPlayerToADefaultGroup) {
                Configs.PLAYERS_CONFIG.updateIterable(PlayersConfig::playerGroups) {
                    it.find { playerGroup -> playerGroup.name == "DEFAULT" }?.players?.add(SimplePlayer(uuid = handler.player.uuidAsString, handler.player.name.string))
                }
            }
        }
    }

    private fun registerCommands() = CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
        HomesCmd().register(dispatcher)
        WarpsCmd.register(dispatcher)
        BackCmd.register(dispatcher)
        ReloadFilesCmd.register(dispatcher)
    }

}