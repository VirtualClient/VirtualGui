package gg.virtualclient.virtualgui.constraints.debug

import gg.virtualclient.virtualgui.UIComponent
import gg.virtualclient.virtualgui.constraints.ConstraintType
import gg.virtualclient.virtualgui.constraints.SuperConstraint

internal class NoopConstraintDebugger : ConstraintDebugger {
    override fun evaluate(constraint: SuperConstraint<Float>, type: ConstraintType, component: UIComponent): Float {
        if (constraint.recalculate) {
            constraint.cachedValue = invokeImpl(constraint, type, component)
            constraint.recalculate = false
        }

        return constraint.cachedValue
    }
}
