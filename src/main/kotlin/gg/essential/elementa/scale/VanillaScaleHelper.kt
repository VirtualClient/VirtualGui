package gg.essential.elementa.scale

import gg.essential.elementa.impl.Platform
import gg.virtualclient.virtualminecraft.VirtualMatrixStack
import gg.virtualclient.virtualminecraft.VirtualWindow

object VanillaScaleHelper : ScaleHelper {
    override fun drawScreen(matrixStack: VirtualMatrixStack) {}

    override fun postDrawScreen(matrixStack: VirtualMatrixStack) {}


    override fun init() {}
    override fun getScaleFactor(): Double = VirtualWindow.scaleFactor
    override fun getScaledWidth(): Int = VirtualWindow.scaledWidth
    override fun getScaledHeight(): Int = VirtualWindow.scaledHeight

}