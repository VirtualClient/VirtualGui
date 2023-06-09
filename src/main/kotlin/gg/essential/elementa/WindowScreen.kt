package gg.essential.elementa

import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.animation.*
import gg.essential.elementa.scale.WindowScaler
import gg.essential.elementa.scale.VanillaScaleHelper
import gg.virtualclient.virtualminecraft.VirtualMatrixStack
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
    component: Component,
    private val enableRepeatKeys: Boolean = true,
    private val drawDefaultBackground: Boolean = true,
    val scaleHelper: WindowScaler = VanillaScaleHelper
) : VirtualScreen(component) {

    val window = Window(scaleHelper = scaleHelper)
    private var isInitialized = false

    init {
//        window.onKeyType { typedChar, keyCode ->
//            defaultKeyBehavior(typedChar, keyCode)
//        }
    }

    open fun afterInitialization() { }

    override fun render(matrices: VirtualMatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        if (!isInitialized) {
            isInitialized = true
            afterInitialization()
        }
        val mX = scaleHelper.getMouseX().toInt()
        val mY = scaleHelper.getMouseY().toInt()

        super.render(matrices, mX, mY, delta)

        if (drawDefaultBackground)
            renderBackground(matrices)

        // Now, we need to hook up Elementa to this GuiScreen. In practice, Elementa
        // is not constrained to being used solely inside of a GuiScreen, all the programmer
        // needs to do is call the [Window] events when appropriate, whenever that may be.
        // In our example, it is in the overridden [GuiScreen#drawScreen] method.
        window.draw(matrices)
    }

    override fun onMouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        //I think this clashes with elementa v2 but without it our scale helper breaks
        val mX = scaleHelper.getMouseX().toDouble()
        val mY = scaleHelper.getMouseY().toDouble()

        super.onMouseClicked(mX, mY, button)

        // Restore decimal value to mouse locations if not present.
        // See [ElementaVersion.V2] for more info
        val (adjustedMouseX, adjustedMouseY) =
            if ((mX == floor(mX) && mY == floor(mY))) {
                val x = window.getMouseX()
                val y = window.getMouseY()

                mX + (x - floor(x)) to mY + (y - floor(y))
            } else {
                mX to mY
            }

        // We also need to pass along clicks
        window.mouseClick(adjustedMouseX, adjustedMouseY, button)
        return true
    }

    override fun onMouseReleased(mouseX: Double, mouseY: Double, state: Int): Boolean {
        val mX = scaleHelper.getMouseX().toDouble()
        val mY = scaleHelper.getMouseY().toDouble()
        super.onMouseReleased(mX, mY, state)

        // We also need to pass along mouse releases
        window.mouseRelease()
        return true
    }

    override fun onMouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
        val mX = scaleHelper.getMouseX().toDouble()
        val mY = scaleHelper.getMouseY().toDouble()
        super.onMouseScrolled(mX, mY, amount)

        // We also need to pass along scrolling
        window.mouseScroll(amount.coerceIn(-1.0, 1.0))
        return true
    }

    override fun onKeyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        super.onKeyPressed(keyCode, scanCode, modifiers)
        // We also need to pass along typed keys
        window.keyType(0.toChar(), keyCode)
        return true
    }

    override fun onCharTyped(chr: Char, modifiers: Int): Boolean {
        super.onCharTyped(chr, modifiers)

        window.keyType(chr, 0)

        return true
    }


    override fun onScreenInit() {
        window.onWindowResize()

        super.onScreenInit()

        // Since we want our users to be able to hold a key
        // to type. This is a wrapper around a base LWJGL function.
        // - Keyboard.enableRepeatEvents in <= 1.12.2
        if (enableRepeatKeys)
            VirtualKeyboard.setRepeatEvents(true)
    }

    override fun onScreenClosed() {
        super.onScreenClosed()

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
