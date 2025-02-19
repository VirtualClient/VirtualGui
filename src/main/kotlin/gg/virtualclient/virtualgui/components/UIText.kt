package gg.virtualclient.virtualgui.components

import gg.virtualclient.virtualgui.UIComponent
import gg.virtualclient.virtualgui.UIConstraints
import gg.virtualclient.virtualgui.constraints.CenterConstraint
import gg.virtualclient.virtualgui.dsl.width
import gg.virtualclient.virtualgui.state.BasicState
import gg.virtualclient.virtualgui.state.MappedState
import gg.virtualclient.virtualgui.state.State
import gg.virtualclient.virtualgui.state.pixels
import gg.virtualclient.virtualminecraft.VirtualMatrixStack
import gg.virtualclient.virtualminecraft.VirtualRenderSystem
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import java.awt.Color

/**
 * Simple text component that draws its given `text` at the scale determined by
 * this component's width & height constraints.
 */
open class UIText constructor(
    text: State<Component>,
    shadow: State<Boolean> = BasicState(true),
    shadowColor: State<Color?> = BasicState(null)
) : UIComponent() {
    constructor(
        text: String = "",
        shadow: Boolean = true,
        shadowColor: Color? = null
    ) : this(BasicState(LegacyComponentSerializer.legacySection().deserialize(text)), BasicState(shadow), BasicState(shadowColor))

    @JvmOverloads constructor(
        text: Component = Component.empty(),
        shadow: Boolean = true,
        shadowColor: Color? = null
    ) : this(BasicState(text), BasicState(shadow), BasicState(shadowColor))


    private val textState: MappedState<Component, Component> = text.map { it } // extra map so we can easily rebind it
    private val shadowState: MappedState<Boolean, Boolean> = shadow.map { it }
    private val shadowColorState: MappedState<Color?, Color?> = shadowColor.map { it }
    private val textScaleState = constraints.asState { getTextScale() }
    /** Guess on whether we should be trying to center or top-align this component. See [BELOW_LINE_HEIGHT]. */
    private val verticallyCenteredState = constraints.asState { y is CenterConstraint }
    private val fontProviderState = constraints.asState { fontProvider }
    private var textWidthState = textState.zip(textScaleState.zip(fontProviderState)).map { (text, opts) ->
        val (textScale, fontProvider) = opts
        text.width(textScale, fontProvider) / textScale
    }



    private fun <T> UIConstraints.asState(selector: UIConstraints.() -> T) = BasicState(selector(constraints)).also {
        constraints.addObserver { _, _ -> it.set(selector(constraints)) }
    }

    init {
        setWidth(textWidthState.pixels())
        setHeight(shadowState.zip(verticallyCenteredState.zip(fontProviderState)).map { (shadow, opts) ->
            val (verticallyCentered, fontProvider) = opts
            val above = (if (verticallyCentered) fontProvider.getBelowLineHeight() else 0f)
            val center = fontProvider.getBaseLineHeight()
            val below = fontProvider.getBelowLineHeight() + (if (shadow) fontProvider.getShadowHeight() else 0f)
            above + center + below
        }.pixels())
    }

    fun bindText(newTextState: State<Component>) = apply {
        textState.rebind(newTextState)
    }

    fun bindShadow(newShadowState: State<Boolean>) = apply {
        shadowState.rebind(newShadowState)
    }

    fun bindShadowColor(newShadowColorState: State<Color?>) = apply {
        shadowColorState.rebind(newShadowColorState)
    }

    fun getText() = textState.get()
    fun setText(text: Component) = apply { textState.set(text) }

    fun setText(text: String) = apply { textState.set(LegacyComponentSerializer.legacySection().deserialize(text)) }


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

    override fun getWidth(): Float {
        return super.getWidth() * getTextScale()
    }

    override fun getHeight(): Float {
        return super.getHeight() * getTextScale()
    }

    override fun draw(matrixStack: VirtualMatrixStack) {
        val text = textState.get()
        if (text == Component.empty())
            return

        beforeDraw(matrixStack)

        val scale = getWidth() / textWidthState.get()
        val x = getLeft()
        val y = getTop() + (if (verticallyCenteredState.get()) fontProviderState.get().getBelowLineHeight() * scale else 0f)
        val color = getColor()

        // We aren't visible, don't draw
        if (color.alpha <= 10) {
            return super.draw(matrixStack)
        }

        VirtualRenderSystem.enableBlend()

        val shadow = shadowState.get()
        val shadowColor = shadowColorState.get()
        getFontProvider().drawString(
            matrixStack,
            textState.get(), color, x, y,
            10f, scale, shadow, shadowColor
        )
        super.draw(matrixStack)
    }


}
