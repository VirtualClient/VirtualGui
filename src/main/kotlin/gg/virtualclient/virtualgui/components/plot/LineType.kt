package gg.essential.elementa.components.plot

import gg.virtualclient.virtualminecraft.VirtualRenderSystem
import org.lwjgl.opengl.GL11
import java.awt.Color

enum class LineType(private val drawFunc: (List<PlotPoint>) -> Unit) {
    Linear({ points ->
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glBegin(GL11.GL_LINE_STRIP)
        points.forEach {
            GL11.glVertex2f(it.x, it.y)
        }
        GL11.glEnd()
    });

    fun draw(points: List<PlotPoint>, color: Color, width: Float) {
        beforeDraw(color, width)
        drawFunc(points)
        afterDraw()
    }

    private fun beforeDraw(color: Color, width: Float) {
        VirtualRenderSystem.enableBlend()
        VirtualRenderSystem.disableTexture2D()

        GL11.glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
        GL11.glLineWidth(width)
    }

    private fun afterDraw() {
        VirtualRenderSystem.enableTexture2D()
    }
}
