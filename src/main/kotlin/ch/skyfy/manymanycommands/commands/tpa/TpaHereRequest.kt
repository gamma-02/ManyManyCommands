package ch.skyfy.manymanycommands.commands.tpa

import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class TpaHereRequest : TpaRequestBase() {
    override fun customMessage(playerName: String): Text {
        return Text.literal("A player wants you to teleport to him. Type ")
            .setStyle(Style.EMPTY.withColor(Formatting.GOLD)).append(
                Text.literal("/tpaaccept $playerName").setStyle(Style.EMPTY.withColor(Formatting.YELLOW)).append(
                    Text.literal(" to accept !").setStyle(Style.EMPTY.withColor(Formatting.GOLD))
                )
            )
    }
}