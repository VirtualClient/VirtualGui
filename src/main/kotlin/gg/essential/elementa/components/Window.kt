package gg.essential.elementa.components

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.constraints.resolution.ConstraintResolutionGui
import gg.essential.elementa.constraints.resolution.ConstraintResolver
import gg.essential.elementa.constraints.resolution.ConstraintResolverV2
import gg.essential.elementa.effects.ScissorEffect
import gg.essential.elementa.font.FontRenderer
import gg.essential.elementa.impl.Platform.Companion.platform
import gg.essential.elementa.scale.ScaleHelper
import gg.essential.elementa.scale.VanillaScaleHelper
import gg.essential.elementa.utils.elementaDev
import gg.essential.elementa.utils.requireMainThread
import gg.virtualclient.virtualminecraft.VirtualMatrixStack
import org.lwjgl.opengl.GL11
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

/**
 * "Root" component. All components MUST have a Window in their hierarchy in order to do any rendering
 * or animating.
 */
class Window @JvmOverloads constructor(
    internal val version: ElementaVersion = ElementaVersion.V2,
    val animationFPS: Int = 244,
    private var scaleHelper: ScaleHelper = VanillaScaleHelper
) : UIComponent() {
    private var systemTime = -1L
    private var currentMouseButton = -1

    private var floatingComponents = mutableListOf<UIComponent>()

    var hoveredFloatingComponent: UIComponent? = null
    var focusedComponent: UIComponent? = null
        private set
    private var componentRequestingFocus: UIComponent? = null

    private var cancelDrawing = false

    internal var clickInterceptor: ((mouseX: Double, mouseY: Double, button: Int) -> Boolean)? = null

    init {
        super.parent = this
    }

    override fun onWindowResize() {
        //So the components that will be initialized/reinitialized here all have the scale helper applied.
        val prevScaleHelper = ScaleHelper.getActiveScaleHelper()
        ScaleHelper.setActiveScaleHelper(scaleHelper)
        scaleHelper.init()

        enqueueRenderOperation {
            //Reinit again on next render call
            scaleHelper.init()
        }

        super.onWindowResize()
        ScaleHelper.setActiveScaleHelper(prevScaleHelper)
    }

    override fun getScaleHelper(): ScaleHelper {
        return scaleHelper
    }

    override fun afterInitialization() {
        enqueueRenderOperation {
            FontRenderer.initShaders()
            UICircle.initShaders()
            UIRoundedRectangle.initShaders()
        }
    }

    override fun draw(matrixStack: VirtualMatrixStack) =
        version.enableFor { doDraw(matrixStack) }

    private fun doDraw(matrixStack: VirtualMatrixStack) {
        if (cancelDrawing)
            return

        requireMainThread()

        val prevScaleHelper = ScaleHelper.getActiveScaleHelper()

        //Just to make sure it is set
        ScaleHelper.setActiveScaleHelper(this.scaleHelper)
        scaleHelper.drawScreen(matrixStack)

        val startTime = System.nanoTime()

        val it = renderOperations.iterator()
        while (it.hasNext() && System.nanoTime() - startTime < TimeUnit.MILLISECONDS.toNanos(5)) {
            it.next()()
            it.remove()
        }

        if (systemTime == -1L)
            systemTime = System.currentTimeMillis()

        try {

            //If this Window is more than 5 seconds behind, reset it be only 5 seconds.
            //This will drop missed frames but avoid the game freezing as the Window tries
            //to catch after a period of inactivity
            if (System.currentTimeMillis() - this.systemTime > TimeUnit.SECONDS.toMillis(5))
                this.systemTime = System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(5)

            while (this.systemTime < System.currentTimeMillis() + 1000 / animationFPS) {
                animationFrame()
                this.systemTime += 1000 / animationFPS
            }

            hoveredFloatingComponent = null
            val (mouseX, mouseY) = getMousePosition()
            for (component in floatingComponents.reversed()) {
                if (component.isPointInside(mouseX, mouseY)) {
                    hoveredFloatingComponent = component
                    break
                }
            }

            mouseMove(this)
            beforeDraw(matrixStack)
            super.draw(matrixStack)
        } catch (e: Throwable) {
            cancelDrawing = true

            val guiName = platform.currentScreen?.javaClass?.simpleName ?: "<unknown>"
            when (e) {
                is StackOverflowError -> {
                    println("Elementa: Cyclic constraint structure detected!")
                    println("If you are a developer, set the environment variable \"elementa.dev=true\" to assist in debugging the issue.")
                }
                else -> {
                    println("Elementa: encountered an error while drawing a GUI")
                }
            }
            println("Gui name: $guiName")
            e.printStackTrace()

            // We may have thrown in the middle of a ScissorEffect, in which case we
            // need to disable the scissor if we don't want half the user's screen gone
            ScissorEffect.currentScissorState = null
            GL11.glDisable(GL11.GL_SCISSOR_TEST)

            platform.currentScreen = when {
                e is StackOverflowError && elementaDev -> {
                    val cyclicNodes = when (System.getProperty("elementa.dev.cycle_resolver", "2")) {
                        "2" -> ConstraintResolverV2(this).getCyclicNodes()
                        "1" -> ConstraintResolver(this).getCyclicNodes()
                        else -> {
                            println("Invalid value for \"elementa.dev.cycle_resolver\", falling back to V2 solver.")
                            ConstraintResolverV2(this).getCyclicNodes()
                        }
                    }
                    ConstraintResolutionGui(guiName, this, cyclicNodes)
                }
                else -> {
                    null
                }
            }
        }
        scaleHelper.postDrawScreen(matrixStack)
        ScaleHelper.setActiveScaleHelper(prevScaleHelper)
    }

    internal fun drawEmbedded(matrixStack: VirtualMatrixStack) {
        scaleHelper.drawScreen(matrixStack)
        super.draw(matrixStack)
        scaleHelper.postDrawScreen(matrixStack)
    }

    fun drawFloatingComponents(matrixStack: VirtualMatrixStack) {
        requireMainThread()

        val it = floatingComponents.iterator()
        while (it.hasNext()) {
            val component = it.next()
            if (ofOrNull(component) == null) {
                it.remove()
                continue
            }
            component.draw(matrixStack)
        }
    }

    override fun mouseScroll(delta: Double) {
        requireMainThread()

        val (mouseX, mouseY) = getMousePosition()
        for (floatingComponent in floatingComponents.reversed()) {
            if (floatingComponent.isPointInside(mouseX, mouseY)) {
                floatingComponent.mouseScroll(delta)
                return
            }
        }

        super.mouseScroll(delta)
    }

    override fun mouseClick(mouseX: Double, mouseY: Double, button: Int) {
        requireMainThread()

        //  Override mouse positions to be in the center of the pixel on Elementa versions
        //  2 and over. See [ElementaVersion.V2] for more info.
        val (adjustedX, adjustedY) = pixelCoordinatesToPixelCenter(mouseX, mouseY)

        doMouseClick(adjustedX, adjustedY, button)
    }

    private fun doMouseClick(mouseX: Double, mouseY: Double, button: Int) {
        currentMouseButton = button

        clickInterceptor?.let {
            if (it(mouseX, mouseY, button)) {
                return
            }
        }

        for (floatingComponent in floatingComponents.reversed()) {
            if (floatingComponent.isPointInside(mouseX.toFloat(), mouseY.toFloat()) && !floatingComponent.ignoreInteractions) {
                floatingComponent.mouseClick(mouseX, mouseY, button)
                dealWithFocusRequests()
                return
            }
        }

        super.mouseClick(mouseX, mouseY, button)
        dealWithFocusRequests()
    }

    private fun dealWithFocusRequests() {
        if (componentRequestingFocus == null) {
            unfocus()
        } else if (componentRequestingFocus != focusedComponent) {
            if (focusedComponent != null)
                focusedComponent?.loseFocus()

            focusedComponent = componentRequestingFocus
            focusedComponent?.focus()
        }

        componentRequestingFocus = null
    }

    override fun mouseRelease() {
        requireMainThread()

        super.mouseRelease()

        currentMouseButton = -1
    }

    override fun keyType(typedChar: Char, keyCode: Int) {
        requireMainThread()

        if (focusedComponent != null) {
            focusedComponent?.keyType(typedChar, keyCode)
        } else {
            super.keyType(typedChar, keyCode)
        }
    }

    override fun animationFrame() {
        if (currentMouseButton != -1) {
            val (mouseX, mouseY) = getMousePosition()
            if (version >= ElementaVersion.v2) {
                dragMouse(mouseX, mouseY, currentMouseButton)
            } else {
                @Suppress("DEPRECATION")
                dragMouse(mouseX.toInt(), mouseY.toInt(), currentMouseButton)
            }
        }

        if (componentRequestingFocus != null && componentRequestingFocus != focusedComponent) {
            if (focusedComponent != null)
                focusedComponent?.loseFocus()

            focusedComponent = componentRequestingFocus
            focusedComponent?.focus()
        }
        componentRequestingFocus = null

        super.animationFrame()
    }

    override fun getLeft(): Float {
        return 0f
    }

    override fun getTop(): Float {
        return 0f
    }

    override fun getWidth(): Float {
        return getScaleHelper().getScaledWidth().toFloat()
    }

    override fun getHeight(): Float {
        return getScaleHelper().getScaledHeight().toFloat()
    }

    override fun getRight() = getWidth()
    override fun getBottom() = getHeight()

    fun isAreaVisible(left: Double, top: Double, right: Double, bottom: Double): Boolean {
        if (right < getLeft() ||
            left > getRight() ||
            bottom < getTop() ||
            top > getBottom()
        ) return false

        val currentScissor = ScissorEffect.currentScissorState ?: return true
        val sf = ScaleHelper.getActiveScaleHelper().getScaleFactor()

        val realX = currentScissor.x / sf
        val realWidth = currentScissor.width / sf

        val bottomY = ((ScaleHelper.getActiveScaleHelper().getScaledHeight() * sf) - currentScissor.y) / sf
        val realHeight = currentScissor.height / sf

        return right > realX &&
                left < realX + realWidth &&
                bottom >= bottomY - realHeight &&
                top <= bottomY
    }

    /*
     * Floating API
     */

    fun addFloatingComponent(component: UIComponent) {
        if (isInitialized) {
            requireMainThread()
        }

        if (floatingComponents.contains(component)) return

        floatingComponents.add(component)
    }

    fun removeFloatingComponent(component: UIComponent) {
        if (isInitialized) {
            requireMainThread()
        }

        floatingComponents.remove(component)
    }

    fun isComponentFloating(component: UIComponent): Boolean {
        return floatingComponents.contains(component)
    }

    fun isFloating(component: UIComponent): Boolean {
        if(isComponentFloating(component))
            return true
        if(!component.hasParent || component.parent == this)
            return false
        return isFloating(component.parent)
    }

    /**
     * Overridden to including floating components.
     */
    override fun hitTest(x: Float, y: Float): UIComponent {
        for (component in floatingComponents.reversed()) {
            if (component.isPointInside(x, y)) {
                return component.hitTest(x, y)
            }
        }
        return super.hitTest(x, y)
    }

    /*
     * Focus API
     */

    /**
     * Focus a component. Focusing means that this component will only propagate keyboard
     * events to the currently focused component. The component to be focused does
     * NOT have to be a direct child of this component.
     */
    fun focus(component: UIComponent) {
        if (isInitialized) {
            requireMainThread()
        }

        componentRequestingFocus = component
    }

    /**
     * Remove the currently focused component. This means only the window will receive
     * keyboard events until another component is focused.
     */
    fun unfocus() {
        if (isInitialized) {
            requireMainThread()
        }

        focusedComponent?.loseFocus()
        focusedComponent = null
    }

    companion object {
        private val renderOperations = ConcurrentLinkedQueue<() -> Unit>()

        fun enqueueRenderOperation(operation: Runnable) {
            renderOperations.add {
                operation.run()
            }
        }

        fun enqueueRenderOperation(operation: () -> Unit) {
            renderOperations.add(operation)
        }

        fun of(component: UIComponent): Window {
            return ofOrNull(component) ?: throw IllegalStateException(
                "No window parent? It's possible you haven't called Window.addChild() at this point in time."
            )
        }

        fun ofOrNull(component: UIComponent): Window? = component.cachedWindow ?: run {
            var current = component

            while (current !is Window && current.hasParent && current.parent != current) {
                current = current.parent
            }

            current as? Window
        }
    }
}
