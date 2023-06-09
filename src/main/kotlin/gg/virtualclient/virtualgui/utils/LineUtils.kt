package gg.virtualclient.virtualgui.utils

import gg.virtualclient.virtualgui.components.UIPoint
import gg.virtualclient.virtualminecraft.VirtualMatrixStack
import gg.virtualclient.virtualminecraft.VirtualRenderSystem
import gg.virtualclient.virtualminecraft.vertex.CommonVertexFormats
import gg.virtualclient.virtualminecraft.vertex.DrawMode
import gg.virtualclient.virtualminecraft.vertex.VirtualBufferBuilder
import java.awt.Color
import kotlin.math.sqrt

object LineUtils {

    @JvmStatic
    fun drawLine(matrixStack: VirtualMatrixStack, x1: Number, y1: Number, x2: Number, y2: Number, color: Color, width: Float) {
        drawLineStrip(matrixStack, listOf(x1 to y1, x2 to y2), color, width)
    }

    @JvmStatic
    fun drawLineStrip(matrixStack: VirtualMatrixStack, points: List<Pair<Number, Number>>, color: Color, width: Float) {
        VirtualRenderSystem.enableBlend()

        val buffer = VirtualBufferBuilder.getFromTessellator()
        buffer.beginWithDefaultShader(DrawMode.TRIANGLE_STRIP, CommonVertexFormats.POSITION_COLOR);
        points.forEachIndexed { index, curr ->
            val (x, y) = curr
            val prev = points.getOrNull(index - 1)
            val next = points.getOrNull(index + 1)
            val (dx, dy) = when {
                prev == null -> next!!.sub(curr)
                next == null -> curr.sub(prev)
                else -> next.sub(prev)
            }
            val dLen = sqrt(dx * dx + dy * dy)
            val nx = dx / dLen * width / 2
            val ny = dy / dLen * width / 2
            buffer.vertex(matrixStack, x.toDouble() + ny, y.toDouble() - nx, 0.0)
                .color(color.red, color.green, color.blue, color.alpha)
                .next()
            buffer.vertex(matrixStack, x.toDouble() - ny, y.toDouble() + nx, 0.0)
                .color(color.red, color.green, color.blue, color.alpha)
                .next()
        }
        VirtualBufferBuilder.drawTessellator()
    }

    private fun Pair<Number, Number>.sub(other: Pair<Number, Number>): Pair<Double, Double> {
        val (x1, y1) = this
        val (x2, y2) = other
        return Pair(x1.toDouble() - x2.toDouble(), y1.toDouble() - y2.toDouble())
    }

    @JvmStatic
    fun drawLine(matrixStack: VirtualMatrixStack, p1: UIPoint, p2: UIPoint, color: Color, width: Float) {
        drawLine(matrixStack, p1.absoluteX, p1.absoluteY, p2.absoluteX, p2.absoluteY, color, width)
    }
}
