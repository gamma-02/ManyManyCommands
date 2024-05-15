package ch.skyfy.manymanycommands.commands.warps

import ch.skyfy.json5configlib.updateIterable
import ch.skyfy.json5configlib.updateIterableNested
import ch.skyfy.manymanycommands.api.config.Configs
import ch.skyfy.manymanycommands.api.persistent.WarpsData
import ch.skyfy.manymanycommands.api.data.Location
import ch.skyfy.manymanycommands.api.data.Warp
import ch.skyfy.manymanycommands.api.data.WarpGroup
import ch.skyfy.manymanycommands.api.persistent.Persistent
import ch.skyfy.manymanycommands.commands.AbstractCommand
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

fun addWarp(
    spe: ServerPlayerEntity,
    warpName: String,
    x: Double = spe.x,
    y: Double = spe.y,
    z: Double = spe.z,
    pitch: Float = spe.pitch,
    yaw: Float = spe.yaw,
    dimension: String = spe.world.dimensionEntry.key.get().value.toString()
) {
    Persistent.WARPS.serializableData.warps.find { it.name == warpName }?.let {
        spe.sendMessage(Text.literal("A Warp with the name $warpName already exist").setStyle(Style.EMPTY.withColor(Formatting.RED)))
        return
    }
    Persistent.WARPS.updateIterable(WarpsData::warps) { it.add(Warp(warpName, Location(x, y, z, pitch, yaw, dimension))) }

    if (Configs.RULES.serializableData.shouldAddNewWarpToTheDefaultGroup) {
//        Configs.WARPS.updateIterable(WarpConfig::groups) { warpGroups ->
//            warpGroups.firstOrNull { it.name == "DEFAULT" }?.warps?.add(warpName)
//        }
        Configs.RULES.serializableData.warpGroups.firstOrNull { it.name == "DEFAULT" }?.let { warpGroup ->
            Configs.RULES.updateIterableNested(WarpGroup::warps, warpGroup.warps) { warps ->
                if(!warps.contains(warpName)) warps.add(warpName)
            }
        }
    }

    spe.sendMessage(Text.literal("Warp of name «$warpName» at coordinate ${String.format("%.2f", x)} ${String.format("%.2f", y)} ${String.format("%.2f", z)} in $dimension has been added").setStyle(Style.EMPTY.withColor(Formatting.GREEN)))
}

class CreateWarp : AbstractCommand() {
    override fun runImpl(context: CommandContext<ServerCommandSource>): Int {
        addWarp(context.source?.player ?: return Command.SINGLE_SUCCESS, StringArgumentType.getString(context, "warpName"))
        return Command.SINGLE_SUCCESS
    }
}

class CreateWarpWithCoordinates : AbstractCommand() {
    override fun runImpl(context: CommandContext<ServerCommandSource>): Int {
        addWarp(
            context.source?.player ?: return Command.SINGLE_SUCCESS,
            warpName = StringArgumentType.getString(context, "warpName"),
            x = DoubleArgumentType.getDouble(context, "x"),
            y = DoubleArgumentType.getDouble(context, "y"),
            z = DoubleArgumentType.getDouble(context, "z"),
            pitch = FloatArgumentType.getFloat(context, "pitch"),
            yaw = FloatArgumentType.getFloat(context, "yaw")
        )
        return Command.SINGLE_SUCCESS
    }
}