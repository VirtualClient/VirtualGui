package gg.virtualclient.virtualgui.constraints

import gg.virtualclient.virtualgui.UIComponent
import gg.virtualclient.virtualgui.constraints.resolution.ConstraintVisitor

class MousePositionConstraint : PositionConstraint {
    override var cachedValue = 0f
    override var recalculate = true
    override var constrainTo: UIComponent? = null

    override fun getXPositionImpl(component: UIComponent): Float {
        return component.getMouseX()
    }

    override fun getYPositionImpl(component: UIComponent): Float {
        return component.getMouseY()
    }

    override fun visitImpl(visitor: ConstraintVisitor, type: ConstraintType) { }
}
