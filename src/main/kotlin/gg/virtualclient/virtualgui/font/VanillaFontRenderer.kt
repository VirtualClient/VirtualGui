package gg.virtualclient.virtualgui.font

import gg.virtualclient.virtualgui.UIComponent
import gg.virtualclient.virtualgui.constraints.ConstraintType
import gg.virtualclient.virtualgui.constraints.resolution.ConstraintVisitor
import gg.virtualclient.virtualgui.utils.roundToRealPixels
import gg.virtualclient.virtualminecraft.VirtualMatrixStack
import gg.virtualclient.virtualminecraft.VirtualTextRenderer
import net.kyori.adventure.text.Component
import java.awt.Color

class VanillaFontRenderer : FontProvider {
    override var cachedValue: FontProvider = this
    override var recalculate: Boolean = false
    override var constrainTo: UIComponent? = null

    override fun visitImpl(visitor: ConstraintVisitor, type: ConstraintType) {
    }

    override fun getStringWidth(string: String, pointSize: Float): Float =
        VirtualTextRenderer.getInstance().getWidth(string).toFloat()


    override fun getStringWidth(string: Component, pointSize: Float): Float =
        VirtualTextRenderer.getInstance().getWidth(string).toFloat()

    override fun getStringHeight(string: String, pointSize: Float): Float =
        VirtualTextRenderer.getInstance().fontHeight.toFloat()

    override fun getStringHeight(string: Component, pointSize: Float): Float =
        VirtualTextRenderer.getInstance().fontHeight.toFloat()

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
        val scaledX = x.roundToRealPixels() / scale
        val scaledY = y.roundToRealPixels() / scale

        matrixStack.scale(scale, scale, 1f)
        if (shadowColor == null) {
            if(shadow) {
                VirtualTextRenderer.getInstance().drawWithShadow(matrixStack, string, scaledX, scaledY, color.rgb)
            } else {
                VirtualTextRenderer.getInstance().draw(matrixStack, string, scaledX, scaledY, color.rgb)
            }
        } else {
            VirtualTextRenderer.getInstance().draw(matrixStack, string, scaledX + 1f, scaledY + 1f, shadowColor.rgb)
            VirtualTextRenderer.getInstance().draw(matrixStack, string, scaledX, scaledY, color.rgb)
        }
        matrixStack.scale(1 / scale, 1 / scale, 1f)
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
        val scaledX = x.roundToRealPixels() / scale
        val scaledY = y.roundToRealPixels() / scale

        matrixStack.scale(scale, scale, 1f)
        if (shadowColor == null) {
            if(shadow) {
                VirtualTextRenderer.getInstance().drawWithShadow(matrixStack, string, scaledX, scaledY, color.rgb)
            } else {
                VirtualTextRenderer.getInstance().draw(matrixStack, string, scaledX, scaledY, color.rgb)
            }
        } else {
            VirtualTextRenderer.getInstance().draw(matrixStack, string, scaledX + 1f, scaledY + 1f, shadowColor.rgb)
            VirtualTextRenderer.getInstance().draw(matrixStack, string, scaledX, scaledY, color.rgb)
        }
        matrixStack.scale(1 / scale, 1 / scale, 1f)
    }

    override fun getBaseLineHeight(): Float {
        return BASE_CHAR_HEIGHT
    }

    override fun getShadowHeight(): Float {
        return SHADOW_HEIGHT;
    }

    override fun getBelowLineHeight(): Float {
        return BELOW_LINE_HEIGHT;
    }

    companion object {
        /** Most (English) capital letters have this height, so this is what we use to center "the line". */
        internal const val BASE_CHAR_HEIGHT = 7f

        /**
         * Some letters have a few extra pixels below the visually centered line (gjpqy).
         * To accommodate these, we need to add extra height at the bottom and the top (to keep the original line
         * centered). This needs special consideration because the font renderer does not consider it, so we need to
         * adjust the position we give to it accordingly.
         * Additionally, adding the space on top make top-alignment difficult, whereas not adding it makes centering
         * difficult, so we use a simple heuristic to determine which one it is we're most likely looking for and then
         * either add just the bottom one or the top one as well.
         */
        internal const val BELOW_LINE_HEIGHT = 1f

        /** Extra height if shadows are enabled. */
        const val SHADOW_HEIGHT = 1f
    }
}