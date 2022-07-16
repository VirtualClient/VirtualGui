package gg.essential.elementa.components

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.constraints.ColorConstraint
import gg.essential.elementa.dsl.toConstraint
import gg.essential.elementa.state.State
import gg.essential.elementa.state.toConstraint
import gg.virtualclient.virtualminecraft.VirtualMatrixStack
import gg.virtualclient.virtualminecraft.VirtualRenderSystem
import gg.virtualclient.virtualminecraft.vertex.CommonVertexFormats
import gg.virtualclient.virtualminecraft.vertex.DrawMode
import gg.virtualclient.virtualminecraft.vertex.VirtualBufferBuilder
import org.lwjgl.opengl.GL11
import java.awt.Color

/**
 * Extremely simple component that simply draws a colored rectangle.
 */
open class UIBlock(colorConstraint: ColorConstraint = Color.WHITE.toConstraint()) : UIComponent() {
    constructor(color: Color) : this(color.toConstraint())

    constructor(colorState: State<Color>) : this(colorState.toConstraint())

    init {
        setColor(colorConstraint)
    }

    override fun draw(matrixStack: VirtualMatrixStack) {
        beforeDraw(matrixStack)

        val x = this.getLeft().toDouble()
        val y = this.getTop().toDouble()
        val x2 = this.getRight().toDouble()
        val y2 = this.getBottom().toDouble()

        drawBlock(matrixStack, x, y, x2, y2)

        super.draw(matrixStack)
    }

    internal open fun drawBlock(matrixStack: VirtualMatrixStack, x: Double, y: Double, x2: Double, y2: Double) {
        val color = getColor()
        if (color.alpha == 0)
            return

        drawBlock(matrixStack, color, x, y, x2, y2)
    }

    companion object {

        fun drawBlock(matrixStack: VirtualMatrixStack, color: Color, x1: Double, y1: Double, x2: Double,
                      y2: Double, useDepth: Boolean = true) {
            VirtualRenderSystem.enableBlend()
            VirtualRenderSystem.tryBlendFuncSeparate(770, 771, 1, 0)

            val buffer = VirtualBufferBuilder.getFromTessellator()
            buffer.beginWithDefaultShader(DrawMode.QUADS, CommonVertexFormats.POSITION_COLOR)
            drawBlock(buffer, matrixStack, color, x1, y1, x2, y2, useDepth)

            VirtualRenderSystem.disableBlend()
        }

        fun drawBlockWithActiveShader(matrixStack: VirtualMatrixStack, color: Color, x1: Double,
                                      y1: Double, x2: Double, y2: Double, useDepth: Boolean = true) {
            val buffer = VirtualBufferBuilder.getFromTessellator()
            buffer.beginWithActiveShader(DrawMode.QUADS, CommonVertexFormats.POSITION_COLOR)
            drawBlock(buffer, matrixStack, color, x1, y1, x2, y2, useDepth)
        }

        private fun drawBlock(worldRenderer: VirtualBufferBuilder, matrixStack: VirtualMatrixStack,
                              color: Color, x1: Double, y1: Double, x2: Double, y2: Double, useDepth: Boolean = true) {
            val red = color.red.toFloat() / 255f
            val green = color.green.toFloat() / 255f
            val blue = color.blue.toFloat() / 255f
            val alpha = color.alpha.toFloat() / 255f

            worldRenderer.vertex(matrixStack, x1, y2, 0.0).color(red, green, blue, alpha).next()
            worldRenderer.vertex(matrixStack, x2, y2, 0.0).color(red, green, blue, alpha).next()
            worldRenderer.vertex(matrixStack, x2, y1, 0.0).color(red, green, blue, alpha).next()
            worldRenderer.vertex(matrixStack, x1, y1, 0.0).color(red, green, blue, alpha).next()

            if (ElementaVersion.active >= ElementaVersion.v1 && useDepth) {
                // At some point MC started enabling its depth test during font rendering but all GUI code is
                // essentially flat and has depth tests disabled. This can cause stuff rendered in the background of the
                // GUI to interfere with text rendered in the foreground because none of the blocks rendered in between
                // will actually write to the depth buffer.
                // So that's what we're doing, resetting the depth buffer in the area where we draw the block.
                VirtualRenderSystem.enableDepth()
                VirtualRenderSystem.depthFunc(GL11.GL_ALWAYS)
                VirtualBufferBuilder.drawTessellator()
                VirtualRenderSystem.disableDepth()
                VirtualRenderSystem.depthFunc(GL11.GL_LEQUAL)
            } else {
                VirtualBufferBuilder.drawTessellator()
            }
        }

        fun drawBlockSized(matrixStack: VirtualMatrixStack, color: Color, x: Double, y: Double, width: Double, height: Double) {
            drawBlock(matrixStack, color, x, y, x + width, y + height)
        }
    }
}
