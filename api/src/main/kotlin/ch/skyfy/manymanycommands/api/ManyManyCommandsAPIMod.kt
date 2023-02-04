package ch.skyfy.manymanycommands.api

import ch.skyfy.json5configlib.ConfigManager
import ch.skyfy.manymanycommands.api.config.Configs
import ch.skyfy.manymanycommands.api.persistent.Persistent
import ch.skyfy.manymanycommands.api.utils.setupConfigDirectory
import ch.skyfy.manymanycommands.api.utils.setupExtensionDirectory
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.file.Path

@Suppress("unused")
class ManyManyCommandsAPIMod : ModInitializer {

    companion object {
        const val MOD_ID: String = "manymanycommands_api"
        val CONFIG_DIRECTORY: Path = FabricLoader.getInstance().configDir.resolve("manymanycommands")
        val EXTENSION_DIRECTORY: Path = CONFIG_DIRECTORY.resolve("extensions")
        val PERSISTENT_DIRECTORY: Path = CONFIG_DIRECTORY.resolve("persistent")
        val LOGGER: Logger = LogManager.getLogger(ManyManyCommandsAPIMod::class.java)
    }

    init {
        setupConfigDirectory()
        setupExtensionDirectory()
        ConfigManager.loadConfigs(arrayOf(Configs.javaClass, Persistent.javaClass))
    }

    override fun onInitialize() {}
}