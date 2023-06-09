package gg.virtualclient.virtualgui.constraints.debug

import gg.virtualclient.virtualgui.UIComponent
import gg.virtualclient.virtualgui.constraints.ConstraintType
import gg.virtualclient.virtualgui.constraints.HeightConstraint
import gg.virtualclient.virtualgui.constraints.RadiusConstraint
import gg.virtualclient.virtualgui.constraints.SuperConstraint
import gg.virtualclient.virtualgui.constraints.WidthConstraint
import gg.virtualclient.virtualgui.constraints.XConstraint
import gg.virtualclient.virtualgui.constraints.YConstraint
import gg.virtualclient.virtualgui.utils.roundToRealPixels

internal interface ConstraintDebugger {
    fun evaluate(constraint: SuperConstraint<Float>, type: ConstraintType, component: UIComponent): Float

    fun invokeImpl(constraint: SuperConstraint<Float>, type: ConstraintType, component: UIComponent): Float =
        when (type) {
            ConstraintType.X -> (constraint as XConstraint).getXPositionImpl(component).roundToRealPixels()
            ConstraintType.Y -> (constraint as YConstraint).getYPositionImpl(component).roundToRealPixels()
            ConstraintType.WIDTH -> (constraint as WidthConstraint).getWidthImpl(component).roundToRealPixels()
            ConstraintType.HEIGHT -> (constraint as HeightConstraint).getHeightImpl(component).roundToRealPixels()
            ConstraintType.RADIUS -> (constraint as RadiusConstraint).getRadiusImpl(component)
            else -> throw UnsupportedOperationException()
        }
}

internal var constraintDebugger: ConstraintDebugger? = null

internal inline fun withDebugger(debugger: ConstraintDebugger, block: () -> Unit) {
    val prevDebugger = constraintDebugger
    constraintDebugger = debugger
    try {
        block()
    } finally {
        constraintDebugger = prevDebugger
    }
}
