package gg.virtualclient.virtualgui.constraints.debug

import gg.virtualclient.virtualgui.UIComponent
import gg.virtualclient.virtualgui.constraints.ConstraintType
import gg.virtualclient.virtualgui.constraints.SuperConstraint

internal class RecalculatingConstraintDebugger(
    private val inner: ConstraintDebugger = NoopConstraintDebugger(),
) : ConstraintDebugger {
    private val visited = mutableSetOf<SuperConstraint<*>>()

    override fun evaluate(constraint: SuperConstraint<Float>, type: ConstraintType, component: UIComponent): Float {
        if (visited.add(constraint)) {
            constraint.recalculate = true
        }
        return inner.evaluate(constraint, type, component)
    }
}