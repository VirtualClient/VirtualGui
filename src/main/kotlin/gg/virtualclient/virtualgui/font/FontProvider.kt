package gg.virtualclient.virtualgui.font

import gg.virtualclient.virtualgui.constraints.SuperConstraint
import gg.virtualclient.virtualminecraft.VirtualMatrixStack
import net.kyori.adventure.text.Component
import java.awt.Color

interface FontProvider : SuperConstraint<FontProvider> {
    fun getStringWidth(string: String, pointSize: Float): Float

    fun getStringHeight(string: String, pointSize: Float): Float

    fun getStringWidth(string: Component, pointSize: Float): Float

    fun getStringHeight(string: Component, pointSize: Float): Float

    fun drawString(
        matrixStack: VirtualMatrixStack,
        string: String,
        color: Color,
        x: Float,
        y: Float,
        originalPointSize: Float, //Unused for MC font
        scale: Float,
        shadow: Boolean = true,
        shadowColor: Color? = null
    )

    fun drawString(
        matrixStack: VirtualMatrixStack,
        string: Component,
        color: Color,
        x: Float,
        y: Float,
        originalPointSize: Float, //Unused for MC font
        scale: Float,
        shadow: Boolean = true,
        shadowColor: Color? = null
    )

    fun getBaseLineHeight(): Float

    fun getShadowHeight(): Float

    fun getBelowLineHeight(): Float
}