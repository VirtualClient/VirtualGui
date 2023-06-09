package gg.essential.elementa.components

import gg.essential.elementa.UIComponent
import gg.essential.elementa.dsl.toConstraint
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.utils.readFromLegacyShader
import gg.virtualclient.virtualminecraft.VirtualMatrixStack
import gg.virtualclient.virtualminecraft.shader.BlendState
import gg.virtualclient.virtualminecraft.shader.Float2Uniform
import gg.virtualclient.virtualminecraft.shader.FloatUniform
import gg.virtualclient.virtualminecraft.shader.VirtualShader
import java.awt.Color

/**
 * Simple component that uses shaders to draw a circle. This component
 * takes a radius instead of a height/width!
 *
 * @param radius circle radius
 * @param color circle color
 * @param steps unused, kept for backwards compatibility
 */
class UICircle @JvmOverloads constructor(radius: Float = 0f, color: Color = Color.WHITE, var steps: Int = 0) :
    UIComponent() {
    init {
        setColor(color.toConstraint())
        setRadius(radius.pixels())
    }

    override fun getLeft(): Float {
        return constraints.getX() - getRadius()
    }

    override fun getTop(): Float {
        return constraints.getY() - getRadius()
    }

    override fun getWidth(): Float {
        return getRadius() * 2
    }

    override fun getHeight(): Float {
        return getRadius() * 2
    }

    override fun isPositionCenter(): Boolean {
        return true
    }

    override fun draw(matrixStack: VirtualMatrixStack) {
        beforeDraw(matrixStack)

        val x = constraints.getX()
        val y = constraints.getY()
        val r = getRadius()

        val color = getColor()
        if (color.alpha == 0) return super.draw(matrixStack)

        drawCircle(matrixStack, x, y, r, color)

        super.draw(matrixStack)
    }

    companion object {
        private lateinit var shader: VirtualShader
        private lateinit var shaderRadiusUniform: FloatUniform
        private lateinit var shaderCenterPositionUniform: Float2Uniform

        fun initShaders() {
            if (::shader.isInitialized)
                return

            shader = VirtualShader.readFromLegacyShader("rect", "circle", BlendState.NORMAL)
            if (!shader.usable) {
                println("Failed to load Elementa UICircle shader")
                return
            }
            shaderRadiusUniform = shader.getFloatUniform("u_Radius")
            shaderCenterPositionUniform = shader.getFloat2Uniform("u_CenterPos")
        }

        fun drawCircle(matrixStack: VirtualMatrixStack, centerX: Float, centerY: Float, radius: Float, color: Color) {
            if (!::shader.isInitialized || !shader.usable)
                return

            shader.bind()
            shaderRadiusUniform.setValue(radius)
            shaderCenterPositionUniform.setValue(centerX, centerY)

            UIBlock.drawBlockWithActiveShader(
                matrixStack,
                color,
                (centerX - radius).toDouble(),
                (centerY - radius).toDouble(),
                (centerX + radius).toDouble(),
                (centerY + radius).toDouble()
            )

            shader.unbind()
        }
    }
}
