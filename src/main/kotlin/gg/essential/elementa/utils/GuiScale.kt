package gg.essential.elementa.utils

import gg.virtualclient.virtualminecraft.VirtualWindow
import kotlin.math.min

enum class GuiScale {
    Auto,
    Small,
    Medium,
    Large,
    VeryLarge;

    companion object {
        var guiScaleOverride = -1

        @JvmStatic
        fun fromNumber(number: Int): GuiScale = values()[number]

        @JvmOverloads
        @JvmStatic
        fun scaleForScreenSize(step: Int = 650): GuiScale {
            if(guiScaleOverride != -1) return fromNumber(guiScaleOverride.coerceIn(0, 4))

            val width = VirtualWindow.framebufferWidth
            val height = VirtualWindow.framebufferHeight
            return fromNumber(min((width / step).coerceIn(1, 4), (height / (step / 16 * 9)).coerceIn(1, 4)))
        }
    }
}