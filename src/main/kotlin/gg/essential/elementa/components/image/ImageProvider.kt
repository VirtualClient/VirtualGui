package gg.essential.elementa.components.image

import gg.virtualclient.virtualminecraft.VirtualMatrixStack
import java.awt.Color

interface ImageProvider {
    /**
     * Render the image provided by this component with the provided attributes.
     *
     * This method is guaranteed to be called from the main thread.
     */
    fun drawImage(matrixStack: VirtualMatrixStack, x: Double, y: Double, width: Double, height: Double, color: Color)
}