package gg.virtualclient.virtualgui.markdown.drawables

import gg.virtualclient.virtualgui.UIComponent
import gg.virtualclient.virtualgui.components.UIImage
import gg.virtualclient.virtualgui.constraints.ConstraintType
import gg.virtualclient.virtualgui.constraints.XConstraint
import gg.virtualclient.virtualgui.constraints.YConstraint
import gg.virtualclient.virtualgui.constraints.resolution.ConstraintVisitor
import gg.virtualclient.virtualgui.dsl.childOf
import gg.virtualclient.virtualgui.dsl.pixels
import gg.virtualclient.virtualgui.dsl.toConstraint
import gg.virtualclient.virtualgui.markdown.DrawState
import gg.virtualclient.virtualgui.markdown.MarkdownComponent
import gg.virtualclient.virtualgui.markdown.selection.ImageCursor
import gg.virtualclient.virtualminecraft.VirtualMatrixStack
import java.awt.Color
import java.net.URL

class ImageDrawable(md: MarkdownComponent, val url: URL, private val fallback: Drawable) : Drawable(md) {
    var selected = false
        set(value) {
            field = value
            if (value) {
                image.setColor(Color(200, 200, 255, 255).toConstraint())
            } else {
                image.setColor(Color.WHITE.toConstraint())
            }
        }

    private lateinit var imageX: ShiftableMDPixelConstraint
    private lateinit var imageY: ShiftableMDPixelConstraint

    private val image = UIImage.ofURL(url) childOf md
    private var hasLoaded = false

    override fun layoutImpl(x: Float, y: Float, width: Float): Layout {
        return if (image.isLoaded) {
            imageX = ShiftableMDPixelConstraint(x, 0f)
            imageY = ShiftableMDPixelConstraint(y, 0f)
            image.setX(imageX)
            image.setY(imageY)

            val aspectRatio = image.imageWidth / image.imageHeight
            val imageWidth = image.imageWidth.coerceAtMost(width)
            val imageHeight = imageWidth / aspectRatio

            image.setWidth(imageWidth.pixels())
            image.setHeight(imageHeight.pixels())

            Layout(x, y, imageWidth, imageHeight)
        } else fallback.layout(x, y, width)
    }

    override fun draw(matrixStack: VirtualMatrixStack, state: DrawState) {
        if (!image.isLoaded) {
            fallback.draw(matrixStack, state)
        } else {
            if (!hasLoaded) {
                hasLoaded = true
                md.layout()
            }

            imageX.shift = state.xShift
            imageY.shift = state.yShift
            image.draw(matrixStack)
        }
    }

    // ImageDrawable mouse selection is managed by ParagraphDrawable#select
    override fun cursorAt(mouseX: Float, mouseY: Float, dragged: Boolean, mouseButton: Int) = throw IllegalStateException("never called")
    override fun cursorAtStart() = ImageCursor(this)
    override fun cursorAtEnd() = ImageCursor(this)

    override fun selectedText(asMarkdown: Boolean): String {
        if (asMarkdown) {
            // TODO: `fallback.selectedText(true)` will be empty since the children aren't
            // marked as selected
            return " ![${fallback.selectedText(true)}]($url) "
        }
        return " $url "
    }

    // TODO: Rename this function?
    override fun hasSelectedText() = selected

    private inner class ShiftableMDPixelConstraint(val base: Float, var shift: Float) : XConstraint, YConstraint {
        override var cachedValue = 0f
        override var recalculate = true
        override var constrainTo: UIComponent? = null

        override fun getXPositionImpl(component: UIComponent) = base + shift
        override fun getYPositionImpl(component: UIComponent) = base + shift

        override fun visitImpl(visitor: ConstraintVisitor, type: ConstraintType) { }
    }
}
