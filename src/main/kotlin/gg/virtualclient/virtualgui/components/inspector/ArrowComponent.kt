package gg.virtualclient.virtualgui.components.inspector

import gg.virtualclient.virtualgui.components.TreeArrowComponent
import gg.virtualclient.virtualgui.components.UIImage
import gg.virtualclient.virtualgui.constraints.CenterConstraint
import gg.virtualclient.virtualgui.dsl.childOf
import gg.virtualclient.virtualgui.dsl.constrain
import gg.virtualclient.virtualgui.dsl.pixels
import gg.virtualclient.virtualminecraft.VirtualMatrixStack

class ArrowComponent(private val empty: Boolean) : TreeArrowComponent() {
    private val closedIcon = UIImage.ofResourceCached("/textures/inspector/square_plus.png").constrain {
        width = 7.pixels
        height = 7.pixels
        x = CenterConstraint()
        y = CenterConstraint()
    }
    private val openIcon = UIImage.ofResourceCached("/textures/inspector/square_minus.png").constrain {
        width = 7.pixels
        height = 7.pixels
        x = CenterConstraint()
        y = CenterConstraint()
    }

    init {
        constrain {
            width = 10.pixels
            height = 10.pixels
        }

        if (!empty)
            closedIcon childOf this
    }

    override fun open() {
        if (!empty)
            replaceChild(openIcon, closedIcon)
    }

    override fun close() {
        if (!empty)
            replaceChild(closedIcon, openIcon)
    }

    override fun draw(matrixStack: VirtualMatrixStack) {
        beforeDraw(matrixStack)
        super.draw(matrixStack)
    }
}