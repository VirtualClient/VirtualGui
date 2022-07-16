package gg.essential.elementa.components

import gg.essential.elementa.state.BasicState
import gg.essential.elementa.state.MappedState
import gg.essential.elementa.state.State
import gg.virtualclient.virtualminecraft.VirtualMatrixStack
import gg.virtualclient.virtualminecraft.VirtualRenderSystem
import gg.virtualclient.virtualminecraft.vertex.CommonVertexFormats
import gg.virtualclient.virtualminecraft.vertex.DrawMode
import gg.virtualclient.virtualminecraft.vertex.VirtualBufferBuilder
import org.lwjgl.opengl.GL11
import java.awt.Color

/**
 * Variant of [UIBlock] with two colours that fade into each other in a
 * gradient pattern.
 */
open class GradientComponent constructor(
    startColor: State<Color>,
    endColor: State<Color>,
    direction: State<GradientDirection>
) : UIBlock(Color(0, 0, 0, 0)) {

    @JvmOverloads constructor(
        startColor: Color = Color.WHITE,
        endColor: Color = Color.WHITE,
        direction: GradientDirection = GradientDirection.TOP_TO_BOTTOM
    ): this(BasicState(startColor), BasicState(endColor), BasicState(direction))

    private val startColorState: MappedState<Color, Color> = startColor.map { it }
    private val endColorState: MappedState<Color, Color> = endColor.map{ it }
    private val directionState: MappedState<GradientDirection, GradientDirection> = direction.map { it }

    fun getStartColor(): Color = startColorState.get()
    fun setStartColor(startColor: Color) = apply {
        startColorState.set(startColor)
    }
    fun bindStartColor(newStartColorState: State<Color>) = apply {
        startColorState.rebind(newStartColorState)
    }

    fun getEndColor(): Color = endColorState.get()
    fun setEndColor(endColor: Color) = apply {
        endColorState.set(endColor)
    }
    fun bindEndColor(newEndColorState: State<Color>) = apply {
        endColorState.rebind(newEndColorState)
    }

    fun getDirection(): GradientDirection = directionState.get()
    fun setDirection(direction: GradientDirection) = apply {
        directionState.set(direction)
    }
    fun bindDirection(newDirectionState: State<GradientDirection>) = apply {
        directionState.rebind(newDirectionState)
    }

    override fun drawBlock(matrixStack: VirtualMatrixStack, x: Double, y: Double, x2: Double, y2: Double) {
        drawGradientBlock(
            matrixStack,
            x,
            y,
            x2,
            y2,
            startColorState.get(),
            endColorState.get(),
            directionState.get()
        )
    }

    enum class GradientDirection {
        TOP_TO_BOTTOM,
        BOTTOM_TO_TOP,
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT;

        fun getGradientColors(startColor: Color, endColor: Color): GradientColors = when (this) {
            TOP_TO_BOTTOM -> GradientColors(startColor, startColor, endColor, endColor)
            BOTTOM_TO_TOP -> GradientColors(endColor, endColor, startColor, startColor)
            LEFT_TO_RIGHT -> GradientColors(startColor, endColor, startColor, endColor)
            RIGHT_TO_LEFT -> GradientColors(endColor, startColor, endColor, startColor)
        }
    }

    data class GradientColors(val topLeft: Color, val topRight: Color, val bottomLeft: Color, val bottomRight: Color)

    companion object {

        /**
         * Draw a rectangle with a gradient effect.
         */
        fun drawGradientBlock(
            matrixStack: VirtualMatrixStack,
            x1: Double,
            y1: Double,
            x2: Double,
            y2: Double,
            startColor: Color,
            endColor: Color,
            direction: GradientDirection
        ) {
            VirtualRenderSystem.enableBlend()
            VirtualRenderSystem.disableAlpha()
            VirtualRenderSystem.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
            VirtualRenderSystem.shadeModel(GL11.GL_SMOOTH)

            val colours = direction.getGradientColors(startColor, endColor)
            val tessellator = VirtualBufferBuilder.getFromTessellator()
            tessellator.beginWithDefaultShader(DrawMode.QUADS, CommonVertexFormats.POSITION_COLOR)
            tessellator.vertex(matrixStack, x2, y1, 0.0).color(colours.topRight).next()
            tessellator.vertex(matrixStack, x1, y1, 0.0).color(colours.topLeft).next()
            tessellator.vertex(matrixStack, x1, y2, 0.0).color(colours.bottomLeft).next()
            tessellator.vertex(matrixStack, x2, y2, 0.0).color(colours.bottomRight).next()
            VirtualBufferBuilder.drawTessellator()

            VirtualRenderSystem.shadeModel(GL11.GL_FLAT)
            VirtualRenderSystem.disableBlend()
            VirtualRenderSystem.enableAlpha()
        }
    }
}