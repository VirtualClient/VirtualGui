package gg.virtualclient.virtualgui.markdown.selection

import gg.virtualclient.virtualgui.components.UIBlock
import gg.virtualclient.virtualgui.markdown.DrawState
import gg.virtualclient.virtualgui.markdown.MarkdownComponent
import gg.virtualclient.virtualgui.markdown.drawables.Drawable
import gg.virtualclient.virtualminecraft.VirtualMatrixStack
import java.awt.Color

abstract class Cursor<T : Drawable>(val target: T) {
    protected open val xBase = target.x
    protected open val yBase = target.y
    protected val height = target.height.toDouble()
    protected val width = height / 9.0

    fun draw(matrixStack: VirtualMatrixStack, state: DrawState) {
        if (!MarkdownComponent.DEBUG)
            return

        UIBlock.drawBlockSized(
            matrixStack,
            Color.RED,
            (xBase + state.xShift).toDouble(),
            (yBase + state.yShift).toDouble(),
            width,
            height
        )
    }

    abstract operator fun compareTo(other: Cursor<*>): Int
}
