package gg.virtualclient.virtualgui.font

import gg.virtualclient.virtualgui.UIComponent
import gg.virtualclient.virtualgui.constraints.ConstraintType
import gg.virtualclient.virtualgui.constraints.resolution.ConstraintVisitor
import gg.virtualclient.virtualgui.font.data.Font
import gg.virtualclient.virtualgui.font.data.Glyph
import gg.virtualclient.virtualminecraft.VirtualMatrixStack
import gg.virtualclient.virtualminecraft.VirtualRenderSystem
import gg.virtualclient.virtualminecraft.vertex.CommonVertexFormats
import gg.virtualclient.virtualminecraft.vertex.DrawMode
import gg.virtualclient.virtualminecraft.vertex.VirtualBufferBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import java.awt.Color
import kotlin.math.max

class BasicFontRenderer(
    private val regularFont: Font
) : FontProvider {

    /* Required by Elementa but unused for this type of constraint */
    override var cachedValue: FontProvider = this
    override var recalculate: Boolean = false
    override var constrainTo: UIComponent? = null


    override fun getStringWidth(string: String, pointSize: Float): Float {
        return getStringDimensions(string, pointSize).first
    }

    override fun getStringWidth(string: Component, pointSize: Float): Float {
        return getStringDimensions(LegacyComponentSerializer.legacySection().serialize(string), pointSize).first
    }

    override fun getStringHeight(string: String, pointSize: Float): Float {
        return getStringDimensions(string, pointSize).second
    }

    override fun getStringHeight(string: Component, pointSize: Float): Float {
        return getStringDimensions(LegacyComponentSerializer.legacySection().serialize(string), pointSize).second
    }

    private fun getStringDimensions(string: String, pointSize: Float): Pair<Float, Float> {
        var width = 0f
        var height = 0f

        /*
            10 point font is the default used in Elementa.
            Adjust the point size based on this font's size.
         */
        val currentPointSize = pointSize / 10 * regularFont.fontInfo.atlas.size

        var i = 0
        while (i < string.length) {
            val char = string[i]

            //Ignore formatting codes for purpose of calculating string dimensions
            if (char == '\u00a7' && i + 1 < string.length) {
                //not handled by this font renderer
                i += 2
                continue
            }

            val glyph = regularFont.fontInfo.glyphs[char.code]
            if (glyph?.atlasBounds == null) {
                i++
                continue
            }
            val planeBounds = glyph.planeBounds

            if (planeBounds != null) {
                height = max((planeBounds.top - planeBounds.bottom) * currentPointSize, height)
            }

            //The last character should not have the whitespace to the right of it
            //Added to the width. Instead, we only add the width of the character
            val lastCorrection = if (i < string.length - 1) 0 else 1

            //The texture atlas is used here because in the context of this implementation of the font renderer
            //we do not need or want the full precision the msdf font renderer exports in. Instead, we care about
            //calculating width based on the texture pixels
            width += (((glyph.atlasBounds.right - glyph.atlasBounds.left - lastCorrection) / regularFont.fontInfo.atlas.size) * currentPointSize)


            i++
        }
        return Pair(width, height)
    }

    fun getLineHeight(pointSize: Float): Float {
        return regularFont.fontInfo.metrics.lineHeight * pointSize
    }

    override fun drawString(
        matrixStack: VirtualMatrixStack,
        string: String,
        color: Color,
        x: Float,
        y: Float,
        originalPointSize: Float,
        scale: Float,
        shadow: Boolean,
        shadowColor: Color?
    ) {
        /*
            10 point font is the default used in Elementa.
            Adjust the point size based on this font's size.
         */
        val scaledPointSize = originalPointSize / 10 * regularFont.fontInfo.atlas.size

        /*
            Moved one pixel up so that the main body of the text is in
            the top left of the component. This change keeps text location
            in the same location as the vanilla font renderer relative to
            a UIText component.
         */
        if (shadow) {
            drawStringNow(
                matrixStack,
                string,
                shadowColor ?: Color(
                    ((color.rgb and 16579836).shr(2)).or((color.rgb).and(-16777216))
                ),
                x + 1,
                y,
                scaledPointSize * scale
            )
        }
        drawStringNow(
            matrixStack,
            string,
            color,
            x,
            y - 1,
            scaledPointSize * scale
        )
    }

    override fun drawString(
        matrixStack: VirtualMatrixStack,
        string: Component,
        color: Color,
        x: Float,
        y: Float,
        originalPointSize: Float,
        scale: Float,
        shadow: Boolean,
        shadowColor: Color?
    ) {
        return drawString(matrixStack, LegacyComponentSerializer.legacySection().serialize(string),
            color, x, y, originalPointSize, scale, shadow, shadowColor)
    }

    override fun getBaseLineHeight(): Float {
        return regularFont.fontInfo.atlas.baseCharHeight
    }

    override fun getShadowHeight(): Float {
        return regularFont.fontInfo.atlas.shadowHeight
    }

    override fun getBelowLineHeight(): Float {
        return regularFont.fontInfo.atlas.belowLineHeight
    }

    private fun drawStringNow(
        matrixStack: VirtualMatrixStack,
        string: String,
        color: Color,
        x: Float,
        y: Float,
        originalPointSize: Float
    ) {
        VirtualRenderSystem.bindTexture(0, regularFont.getTexture().dynamicGlId)

        var currentX = x
        var i = 0
        while (i < string.length) {
            val char = string[i]

            // Ignore color code characters in this font renderer
            if (char == '\u00a7' && i + 1 < string.length) {
                i += 2
                continue
            }


            val glyph = regularFont.fontInfo.glyphs[char.code]
            if (glyph == null) {
                i++
                continue
            }

            val planeBounds = glyph.planeBounds

            if (planeBounds != null) {
                val width = (planeBounds.right - planeBounds.left) * originalPointSize
                val height = (planeBounds.top - planeBounds.bottom) * originalPointSize

                drawGlyph(
                    matrixStack,
                    glyph,
                    color,
                    currentX,
                    y + planeBounds.bottom * originalPointSize,
                    width,
                    height
                )
            }

            //The texture atlas is used here because in the context of this implementation of the font renderer
            //we do not need or want the full precision the msdf font renderer exports in. Instead, we care about
            //calculating width based on the texture pixels
            if (glyph.atlasBounds != null) {
                currentX += (((glyph.atlasBounds.right - glyph.atlasBounds.left) / regularFont.fontInfo.atlas.size) * originalPointSize)
            } else {
                currentX += (glyph.advance) * originalPointSize
            }
            i++
        }

    }


    private fun drawGlyph(
        matrixStack: VirtualMatrixStack,
        glyph: Glyph,
        color: Color,
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ) {
        val atlasBounds = glyph.atlasBounds ?: return
        val atlas = regularFont.fontInfo.atlas
        val textureTop = 1.0 - ((atlasBounds.top) / atlas.height)
        val textureBottom = 1.0 - ((atlasBounds.bottom) / atlas.height)
        val textureLeft = (atlasBounds.left / atlas.width).toDouble()
        val textureRight = (atlasBounds.right / atlas.width).toDouble()

        val worldRenderer = VirtualBufferBuilder.getFromTessellator()
        worldRenderer.beginWithDefaultShader(DrawMode.QUADS, CommonVertexFormats.POSITION_TEXTURE_COLOR)
        val doubleX = x.toDouble()
        val doubleY = y.toDouble()
        worldRenderer.vertex(matrixStack, doubleX, doubleY + height, 0.0).texture(textureLeft.toFloat(), textureBottom.toFloat()).color(
            color.red,
            color.green,
            color.blue,
            255
        ).next()
        worldRenderer.vertex(matrixStack, doubleX + width, doubleY + height, 0.0).texture(textureRight.toFloat(), textureBottom.toFloat()).color(
            color.red,
            color.green,
            color.blue,
            255
        ).next()
        worldRenderer.vertex(matrixStack, doubleX + width, doubleY, 0.0).texture(textureRight.toFloat(), textureTop.toFloat()).color(
            color.red,
            color.green,
            color.blue,
            255
        ).next()
        worldRenderer.vertex(matrixStack, doubleX, doubleY, 0.0).texture(textureLeft.toFloat(), textureTop.toFloat()).color(
            color.red,
            color.green,
            color.blue,
            255
        ).next()
        VirtualBufferBuilder.drawTessellator()

    }

    override fun visitImpl(visitor: ConstraintVisitor, type: ConstraintType) {

    }
}