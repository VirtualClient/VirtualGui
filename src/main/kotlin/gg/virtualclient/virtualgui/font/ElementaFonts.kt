package gg.essential.elementa.font

import gg.essential.elementa.font.data.Font

object ElementaFonts {
    private val JETBRAINS_MONO_FONT = Font.fromResource("/fonts/JetBrainsMono-Regular")

    @JvmStatic
    val JETBRAINS_MONO = FontRenderer(JETBRAINS_MONO_FONT)


}
