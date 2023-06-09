package gg.virtualclient.virtualgui.components

import gg.virtualclient.virtualgui.UIComponent
import gg.virtualclient.virtualminecraft.VirtualMatrixStack

/**
 * Bare-bones component that does no rendering and simply offers a bounding box.
 */
open class UIContainer : UIComponent() {
    override fun draw(matrixStack: VirtualMatrixStack) {
        // This is necessary because if it isn't here, effects will never be applied.
        beforeDraw(matrixStack)

        // no-op

        super.draw(matrixStack)
    }
}