package gg.virtualclient.virtualgui.constraints.debug

import gg.virtualclient.virtualgui.UIComponent
import gg.virtualclient.virtualgui.constraints.ConstraintType
import gg.virtualclient.virtualgui.constraints.SuperConstraint

internal class CycleSafeConstraintDebugger(
    private val inner: ConstraintDebugger = NoopConstraintDebugger(),
) : ConstraintDebugger {
    private val stack = mutableSetOf<SuperConstraint<*>>()

    override fun evaluate(constraint: SuperConstraint<Float>, type: ConstraintType, component: UIComponent): Float {
        if (constraint in stack) {
            // Cycle detected! Let's just return some arbitrary value so we can continue.
            return 10f // non-0 so the component can still be highlighted
        }

        stack.add(constraint)
        try {
            return inner.evaluate(constraint, type, component)
        } finally {
            stack.remove(constraint)
        }
    }
}