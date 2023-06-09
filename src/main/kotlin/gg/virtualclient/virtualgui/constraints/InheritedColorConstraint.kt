package gg.virtualclient.virtualgui.constraints

import gg.virtualclient.virtualgui.UIComponent
import gg.virtualclient.virtualgui.constraints.resolution.ConstraintVisitor
import java.awt.Color

class InheritedColorConstraint : ColorConstraint {
    override var cachedValue = Color.WHITE
    override var recalculate = true
    override var constrainTo: UIComponent? = null

    override fun getColorImpl(component: UIComponent): Color {
        return (constrainTo ?: component.parent).getColor()
    }

    // Color constraints will only ever have parent dependencies, so there is no possibility
    // of an invalid constraint here
    override fun visitImpl(visitor: ConstraintVisitor, type: ConstraintType) { }
}
