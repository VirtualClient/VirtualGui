package gg.virtualclient.virtualgui.scale

import gg.virtualclient.virtualgui.impl.Platform
import gg.virtualclient.virtualgui.state.BasicState
import gg.virtualclient.virtualgui.state.State
import gg.virtualclient.virtualgui.utils.GuiScale
import gg.virtualclient.virtualminecraft.VirtualMatrixStack
import gg.virtualclient.virtualminecraft.VirtualWindow

class CustomScaler(private val scaleProvider: State<Int>) : WindowScaler {

    private var scaleFactor: Double = 0.0
    private var scaledWidth: Int = 0
    private var scaledHeight: Int = 0

    init { init() }

    override fun drawScreen(matrixStack: VirtualMatrixStack) {
        val scaledWidth: Double = VirtualWindow.framebufferWidth.toDouble() / scaleFactor
        val scaledHeight: Double = VirtualWindow.framebufferHeight.toDouble() / scaleFactor
        Platform.platform.scale(scaledWidth, scaledHeight)
    }

    private fun calculateScaleFactor(guiScale: Int, forceUnicodeFont: Boolean): Int {
        var i = 1
        while (i != guiScale && i < VirtualWindow.framebufferWidth && i < VirtualWindow.framebufferHeight
            && VirtualWindow.framebufferWidth / (i + 1) >= 320 && VirtualWindow.framebufferHeight / (i + 1) >= 240) {
            ++i
        }
        if (forceUnicodeFont && i % 2 != 0) {
            ++i
        }
        return i
    }

    override fun postDrawScreen(matrixStack: VirtualMatrixStack) {
        Platform.platform.scale(VirtualWindow.framebufferWidth / VirtualWindow.scaleFactor,
            VirtualWindow.framebufferHeight / VirtualWindow.scaleFactor)
    }

    override fun init() {
        this.scaleFactor = calculateScaleFactor(scaleProvider.get(), Platform.platform.forceUnicodeFont).toDouble()
        val i = (VirtualWindow.framebufferWidth.toDouble() / scaleFactor).toInt()
        scaledWidth = if (VirtualWindow.framebufferWidth.toDouble() / scaleFactor > i.toDouble()) i + 1 else i
        val j = (VirtualWindow.framebufferHeight.toDouble() / scaleFactor).toInt()
        scaledHeight = if (VirtualWindow.framebufferHeight.toDouble() / scaleFactor > j.toDouble()) j + 1 else j

    }

    override fun getScaleFactor(): Double {
        return scaleFactor
    }

    override fun getScaledWidth(): Int {
        return scaledWidth
    }

    override fun getScaledHeight(): Int {
        return scaledHeight
    }

    companion object {
        fun scaleForScreenSize(step: Int = 650): CustomScaler {
            return CustomScaler(BasicState(GuiScale.scaleForScreenSize(step).ordinal))
        }
    }
}