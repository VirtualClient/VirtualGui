package gg.virtualclient.virtualgui.markdown.drawables

import gg.virtualclient.virtualgui.markdown.DrawState
import gg.virtualclient.virtualgui.markdown.MarkdownComponent
import gg.virtualclient.virtualgui.markdown.selection.Cursor
import gg.virtualclient.virtualminecraft.VirtualMatrixStack

/**
 * A soft break is one line break between lines of markdown text.
 */
class SoftBreakDrawable(md: MarkdownComponent) : Drawable(md) {
    override fun layoutImpl(x: Float, y: Float, width: Float): Layout {
        TODO("Not yet implemented")
    }

    override fun draw(matrixStack: VirtualMatrixStack, state: DrawState) {
        TODO("Not yet implemented")
    }

    override fun cursorAt(mouseX: Float, mouseY: Float, dragged: Boolean, mouseButton: Int): Cursor<*> {
        TODO("Not yet implemented")
    }

    override fun cursorAtStart(): Cursor<*> {
        TODO("Not yet implemented")
    }

    override fun cursorAtEnd(): Cursor<*> {
        TODO("Not yet implemented")
    }

    override fun selectedText(asMarkdown: Boolean): String {
        TODO("Not yet implemented")
    }
}
