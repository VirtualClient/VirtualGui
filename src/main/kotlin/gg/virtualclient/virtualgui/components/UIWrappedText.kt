package gg.virtualclient.virtualgui.components

import gg.virtualclient.virtualgui.UIComponent
import gg.virtualclient.virtualgui.UIConstraints
import gg.virtualclient.virtualgui.constraints.CenterConstraint
import gg.virtualclient.virtualgui.dsl.basicHeightConstraint
import gg.virtualclient.virtualgui.dsl.width
import gg.virtualclient.virtualgui.state.BasicState
import gg.virtualclient.virtualgui.state.MappedState
import gg.virtualclient.virtualgui.state.State
import gg.virtualclient.virtualgui.state.pixels
import gg.virtualclient.virtualgui.utils.getStringSplitToWidth
import gg.virtualclient.virtualgui.utils.getStringSplitToWidthTruncated
import gg.virtualclient.virtualminecraft.VirtualMatrixStack
import gg.virtualclient.virtualminecraft.VirtualRenderSystem
import gg.virtualclient.virtualminecraft.VirtualTextRenderer
import java.awt.Color

/**
 * Simple text component that draws its given `text` at the scale determined by
 * this component's width & height constrains.
 */
open class UIWrappedText @JvmOverloads constructor(
    text: State<String>,
    shadow: State<Boolean> = BasicState(true),
    shadowColor: State<Color?> = BasicState(null),
    private var centered: Boolean = false,
    /**
     * Keeps the rendered text within the bounds of the component,
     * inserting an ellipsis ("...") if text is trimmed
     */
    private val trimText: Boolean = false,
    private val lineSpacing: Float = 9f,
    private val trimmedTextSuffix: String = "..."
) : UIComponent() {
    @JvmOverloads constructor(
        text: String = "",
        shadow: Boolean = true,
        shadowColor: Color? = null,
        centered: Boolean = false,
        /**
         * Keeps the rendered text within the bounds of the component,
         * inserting an ellipsis ("...") if text is trimmed
         */
        trimText: Boolean = false,
        lineSpacing: Float = 9f,
        trimmedTextSuffix: String = "..."
    ) : this(BasicState(text), BasicState(shadow), BasicState(shadowColor), centered, trimText, lineSpacing, trimmedTextSuffix)


    private val textState: MappedState<String, String> = text.map { it } // extra map so we can easily rebind it
    private val shadowState: MappedState<Boolean, Boolean> = shadow.map { it }
    private val shadowColorState: MappedState<Color?, Color?> = shadowColor.map { it }
    private val textScaleState = constraints.asState { getTextScale() }
    private val fontProviderState = constraints.asState { fontProvider }
    private var textWidthState = textState.zip(textScaleState.zip(fontProviderState)).map { (text, opts) ->
        val (textScale, fontProvider) = opts
        text.width(textScale, fontProvider) / textScale
    }

    private val charWidth = VirtualTextRenderer.getInstance().getWidth("x")

    /** Guess on whether we should be trying to center or top-align this component. See [BELOW_LINE_HEIGHT]. */
    private val verticallyCenteredState = constraints.asState { y is CenterConstraint }

    private fun <T> UIConstraints.asState(selector: UIConstraints.() -> T) = BasicState(selector(constraints)).also {
        constraints.addObserver { _, _ -> it.set(selector(constraints)) }
    }

    /**
     * Balances out space required below the line by adding empty space above the first one.
     * Also, if there are no shadows, the last line can be shorter so it looks more centered overall.
     */
    private val extraHeightState = fontProviderState.zip(verticallyCenteredState).zip(shadowState).map { (opts, shadow) ->
        val (fontProvider, verticallyCentered) = opts
        (if (verticallyCentered) fontProvider.getBelowLineHeight() else 0f) + (if (shadow) 0f else - fontProvider.getShadowHeight())
    }

    init {
        setWidth(textWidthState.pixels())
        setHeight(basicHeightConstraint {
            val fontProvider = super.getFontProvider()

            val lines = getStringSplitToWidth(
                getText(),
                getWidth(),
                getTextScale(),
                ensureSpaceAtEndOfLines = false,
                fontProvider = fontProvider,
            )
            if (lines.isEmpty()) {
                return@basicHeightConstraint 0f
            }

            // The height of the last line of text should be equal the size of that text
            // independent of the lineSpacing property. Otherwise, when lineSpacing is greater
            // than the text height the component's size will be larger than the area the text
            // is rendered
            ((lines.size - 1) * lineSpacing + extraHeightState.get() // All lines but last
                + (fontProvider.getBaseLineHeight() + fontProvider.getBelowLineHeight() + fontProvider.getShadowHeight()) // Last line
                ) * getTextScale()
        })
    }

    fun bindText(newTextState: State<String>) = apply {
        textState.rebind(newTextState)
    }

    fun bindShadow(newShadowState: State<Boolean>) = apply {
        shadowState.rebind(newShadowState)
    }

    fun bindShadowColor(newShadowColorState: State<Color?>) = apply {
        shadowColorState.rebind(newShadowColorState)
    }

    fun getText() = textState.get()
    fun setText(text: String) = apply { textState.set(text) }

    fun getShadow() = shadowState.get()
    fun setShadow(shadow: Boolean) = apply { shadowState.set(shadow) }

    @Deprecated("Wrong return type", level = DeprecationLevel.HIDDEN)
    @JvmName("getShadowColor")
    fun getShadowColorState(): State<Color?> = shadowColorState

    fun getShadowColor(): Color? = shadowColorState.get()
    fun setShadowColor(shadowColor: Color?) = apply { shadowColorState.set(shadowColor) }

    /**
     * Returns the text width if no scale is applied to the text
     */
    fun getTextWidth() = textWidthState.get()

    override fun draw(matrixStack: VirtualMatrixStack) {
        beforeDraw(matrixStack)

        val textScale = getTextScale()
        val x = getLeft()
        val y = getTop() + (if (verticallyCenteredState.get()) fontProviderState.get().getBelowLineHeight() * textScale else 0f)
        val width = getWidth()
        val color = getColor()

        // We aren't visible, don't draw
        if (color.alpha <= 10) {
            return super.draw(matrixStack)
        }

        if (width / textScale <= charWidth) {
            // If we are smaller than a char, we can't physically split this string into
            // "width" strings, so we'll prefer a no-op to an error.
            return super.draw(matrixStack)
        }

        VirtualRenderSystem.enableBlend()

        val lines = if (trimText) {
            getStringSplitToWidthTruncated(
                textState.get(),
                width,
                textScale,
                getMaxLines(),
                ensureSpaceAtEndOfLines = false,
                fontProvider = getFontProvider(),
                trimmedTextSuffix = trimmedTextSuffix
            )
        } else {
            getStringSplitToWidth(
                textState.get(),
                width,
                textScale,
                ensureSpaceAtEndOfLines = false,
                fontProvider = getFontProvider()
            )
        }.map { it.trimEnd() }

        val shadow = shadowState.get()
        val shadowColor = shadowColorState.get()

        lines.forEachIndexed { i, line ->
            val xOffset = if (centered) {
                (width - line.width(textScale)) / 2f
            } else 0f

            getFontProvider().drawString(
                matrixStack,
                line,
                color,
                x + xOffset,
                y + i * lineSpacing * textScale,
                10f,
                textScale,
                shadow,
                if (shadow) shadowColor else null,
            )
        }

        super.draw(matrixStack)
    }

    private fun getMaxLines(): Int {
        val fontProvider = getFontProvider()
        val height = getHeight() / getTextScale() - extraHeightState.get()
        val baseLineHeight = fontProvider.getBaseLineHeight() + fontProvider.getBelowLineHeight() + fontProvider.getShadowHeight()

        if (height < baseLineHeight) {
            return 0
        }

        return 1 + ((height - baseLineHeight) / lineSpacing).toInt()
    }
}
