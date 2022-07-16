package gg.essential.elementa

import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.animation.*
import gg.virtualclient.virtualminecraft.VirtualMatrixStack
import gg.virtualclient.virtualminecraft.VirtualMouse
import gg.virtualclient.virtualminecraft.VirtualScreen
import gg.virtualclient.virtualminecraft.keyboard.VirtualKeyboard
import net.kyori.adventure.text.Component

import java.awt.Color
import kotlin.math.floor
import kotlin.reflect.KMutableProperty0

/**
 * Version of [UScreen] with a [Window] provided and a few useful
 * functions for Elementa Gui programming.
 */
abstract class WindowScreen @JvmOverloads constructor(
    private val version: ElementaVersion = ElementaVersion.V2,
    private val enableRepeatKeys: Boolean = true,
    private val drawDefaultBackground: Boolean = true,
) : VirtualScreen(Component.empty()) {
    val window = Window(version)
    private var isInitialized = false

    init {
//        window.onKeyType { typedChar, keyCode ->
//            defaultKeyBehavior(typedChar, keyCode)
//        }
    }

    open fun afterInitialization() { }

    override fun render(matrixStack: VirtualMatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!isInitialized) {
            isInitialized = true
            afterInitialization()
        }

        super.render(matrixStack, mouseX, mouseY, partialTicks)

        if (drawDefaultBackground)
            renderBackground(matrixStack)

        // Now, we need to hook up Elementa to this GuiScreen. In practice, Elementa
        // is not constrained to being used solely inside of a GuiScreen, all the programmer
        // needs to do is call the [Window] events when appropriate, whenever that may be.
        // In our example, it is in the overridden [GuiScreen#drawScreen] method.
        window.draw(matrixStack)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int): Boolean {
        super.mouseClicked(mouseX, mouseY, mouseButton)

        // Restore decimal value to mouse locations if not present.
        // See [ElementaVersion.V2] for more info
        val (adjustedMouseX, adjustedMouseY) =
            if (version >= ElementaVersion.v2 && (mouseX == floor(mouseX) && mouseY == floor(mouseY))) {
                val x = VirtualMouse.scaledX
                val y = VirtualMouse.scaledY

                mouseX + (x - floor(x)) to mouseY + (y - floor(y))
            } else {
                mouseX to mouseY
            }

        // We also need to pass along clicks
        window.mouseClick(adjustedMouseX, adjustedMouseY, mouseButton)
        return true
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, state: Int): Boolean {
        super.mouseReleased(mouseX, mouseY, state)

        // We also need to pass along mouse releases
        window.mouseRelease()
        return true
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
        super.mouseScrolled(mouseX, mouseY, amount)

        // We also need to pass along scrolling
        window.mouseScroll(amount.coerceIn(-1.0, 1.0))
        return true
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        super.keyPressed(keyCode, scanCode, modifiers)
        // We also need to pass along typed keys
        window.keyType(0.toChar(), keyCode)
        return true
    }

    override fun charTyped(chr: Char, modifiers: Int): Boolean {
        super.charTyped(chr, modifiers)

        window.keyType(chr, 0)

        return true
    }


    override fun init() {
        window.onWindowResize()

        super.init()

        // Since we want our users to be able to hold a key
        // to type. This is a wrapper around a base LWJGL function.
        // - Keyboard.enableRepeatEvents in <= 1.12.2
        if (enableRepeatKeys)
            VirtualKeyboard.setRepeatEvents(true)
    }

    override fun onClose() {
        super.onClose()

        // We need to disable repeat events when leaving the gui.
        if (enableRepeatKeys)
            VirtualKeyboard.setRepeatEvents(false)
    }

//    fun defaultKeyBehavior(typedChar: Char, keyCode: Int) {
//        super.onKeyPressed(keyCode, typedChar, UKeyboard.getModifiers())
//    }

    /**
     * Field animation API
     */

    fun KMutableProperty0<Int>.animate(strategy: AnimationStrategy, time: Float, newValue: Int, delay: Float = 0f) {
        window.apply { this@animate.animate(strategy, time, newValue, delay) }
    }

    fun KMutableProperty0<Float>.animate(strategy: AnimationStrategy, time: Float, newValue: Float, delay: Float = 0f) {
        window.apply { this@animate.animate(strategy, time, newValue, delay) }
    }

    fun KMutableProperty0<Long>.animate(strategy: AnimationStrategy, time: Float, newValue: Long, delay: Float = 0f) {
        window.apply { this@animate.animate(strategy, time, newValue, delay) }
    }

    fun KMutableProperty0<Double>.animate(strategy: AnimationStrategy, time: Float, newValue: Double, delay: Float = 0f) {
        window.apply { this@animate.animate(strategy, time, newValue, delay) }
    }

    fun KMutableProperty0<Color>.animate(strategy: AnimationStrategy, time: Float, newValue: Color, delay: Float = 0f) {
        window.apply { this@animate.animate(strategy, time, newValue, delay) }
    }

    fun KMutableProperty0<*>.stopAnimating() {
        window.apply { this@stopAnimating.stopAnimating() }
    }
}
