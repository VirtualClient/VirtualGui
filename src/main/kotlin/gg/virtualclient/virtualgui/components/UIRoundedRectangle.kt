package gg.essential.elementa.components

import gg.essential.elementa.UIComponent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.utils.readFromLegacyShader
import gg.virtualclient.virtualminecraft.VirtualMatrixStack
import gg.virtualclient.virtualminecraft.shader.BlendState
import gg.virtualclient.virtualminecraft.shader.Float4Uniform
import gg.virtualclient.virtualminecraft.shader.FloatUniform
import gg.virtualclient.virtualminecraft.shader.VirtualShader
import java.awt.Color

/**
 * Alternative to [UIBlock] with rounded corners.
 *
 * @param radius corner radius.
 */
open class UIRoundedRectangle(radius: Float) : UIComponent() {
    init {
        setRadius(radius.pixels())
    }

    override fun draw(matrixStack: VirtualMatrixStack) {
        beforeDraw(matrixStack)

        val radius = getRadius()

        val color = getColor()
        if (color.alpha != 0)
            drawRoundedRectangle(matrixStack, getLeft(), getTop(), getRight(), getBottom(), radius, color)

        super.draw(matrixStack)
    }

    companion object {
        lateinit var shader: VirtualShader
        lateinit var shaderRadiusUniform: FloatUniform
        lateinit var shaderInnerRectUniform: Float4Uniform

        fun initShaders() {
            if (::shader.isInitialized)
                return

            shader = VirtualShader.readFromLegacyShader("rect", "rounded_rect", BlendState.NORMAL)
            if (!shader.usable) {
                println("Failed to load Elementa UIRoundedRectangle shader")
                return
            }
            shaderRadiusUniform = shader.getFloatUniform("u_Radius")
            shaderInnerRectUniform = shader.getFloat4Uniform("u_InnerRect")
        }

        fun isShaderInitialized(): Boolean {
            return ::shader.isInitialized
        }

        /**
         * Draws a rounded rectangle
         */
        fun drawRoundedRectangle(matrixStack: VirtualMatrixStack, left: Float, top: Float, right: Float, bottom: Float, radius: Float, color: Color) {
            if (!::shader.isInitialized || !shader.usable)
                return

            shader.bind()
            shaderRadiusUniform.setValue(radius)
            shaderInnerRectUniform.setValue(left + radius, top + radius, right - radius, bottom - radius)

            UIBlock.drawBlockWithActiveShader(matrixStack, color, left.toDouble(), top.toDouble(), right.toDouble(), bottom.toDouble())

            shader.unbind()
        }
    }
}
