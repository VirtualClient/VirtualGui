package gg.virtualclient.virtualgui.font

import gg.virtualclient.virtualgui.font.data.Font

object ElementaFonts {
    private val JETBRAINS_MONO_FONT = Font.fromResource("/fonts/JetBrainsMono-Regular")

    @JvmStatic
    val JETBRAINS_MONO = FontRenderer(JETBRAINS_MONO_FONT)


}
