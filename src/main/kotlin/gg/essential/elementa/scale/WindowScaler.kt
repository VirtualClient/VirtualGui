package gg.essential.elementa.scale

import gg.virtualclient.virtualminecraft.VirtualMatrixStack
import gg.virtualclient.virtualminecraft.VirtualMouse
import gg.virtualclient.virtualminecraft.VirtualWindow

interface WindowScaler {

    fun drawScreen(matrixStack: VirtualMatrixStack)
    fun postDrawScreen(matrixStack: VirtualMatrixStack)

    fun init()

    fun getScaleFactor() : Double

    fun getScaledWidth() : Int
    fun getScaledHeight() : Int

    fun getMouseY(): Float {
        return ((VirtualMouse.rawY * getScaledHeight().toDouble()) / VirtualWindow.windowHeight.toDouble()).toFloat()
    }

    fun getMouseX(): Float {
        return ((VirtualMouse.rawX * getScaledWidth().toDouble()) / VirtualWindow.windowWidth.toDouble()).toFloat()
    }

    companion object {
        private var activeScaleHelper: WindowScaler? = null

        fun getActiveScaleHelper(): WindowScaler {
            return activeScaleHelper ?: VanillaScaleHelper
        }

        fun setActiveScaleHelper(scaleHelper: WindowScaler?) {
            activeScaleHelper = scaleHelper
        }
    }

}