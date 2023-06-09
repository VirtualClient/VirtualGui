package gg.virtualclient.virtualgui.effects

import gg.virtualclient.virtualgui.UIComponent
import gg.virtualclient.virtualgui.constraints.ColorConstraint
import gg.virtualclient.virtualgui.constraints.ConstraintType
import gg.virtualclient.virtualgui.constraints.resolution.ConstraintVisitor
import gg.virtualclient.virtualgui.dsl.constrain
import gg.virtualclient.virtualgui.state.BasicState
import gg.virtualclient.virtualgui.state.MappedState
import gg.virtualclient.virtualgui.state.State
import gg.virtualclient.virtualgui.utils.withAlpha
import java.awt.Color
import kotlin.math.roundToInt

/**
 * Fades a component's color as well as all of its children.
 */
class RecursiveFadeEffect constructor(
    isOverridden: State<Boolean> = BasicState(false),
    overriddenAlphaPercentage: State<Float> = BasicState(1f)
) : Effect() {
    private val isOverridden: MappedState<Boolean, Boolean> = isOverridden.map { it }
    private val overriddenAlphaPercentage: MappedState<Float, Float> = overriddenAlphaPercentage.map { it }

    fun rebindIsOverridden(state: State<Boolean>) = apply {
        isOverridden.rebind(state)
    }

    fun rebindOverriddenAlphaPercentage(state: State<Float>) = apply {
        overriddenAlphaPercentage.rebind(state)
    }

    override fun setup() {
        recurseChildren(boundComponent) {
            it.constrain {
                color = OverridableAlphaColorConstraint(color, isOverridden, overriddenAlphaPercentage)
            }
        }
    }

    fun remove() {
        recurseChildren(boundComponent) {
            if (it.constraints.color is OverridableAlphaColorConstraint) {
                it.constrain {
                    color = (color as OverridableAlphaColorConstraint).originalConstraint
                }
            }
        }
    }

    private fun recurseChildren(component: UIComponent, action: (UIComponent) -> Unit) {
        action(component)
        component.children.forEach { recurseChildren(it, action) }
    }

    private class OverridableAlphaColorConstraint(
        val originalConstraint: ColorConstraint,
        private val isOverridden: State<Boolean>,
        private val overriddenAlphaPercentage: State<Float>
    ) : ColorConstraint {
        override var cachedValue: Color = Color.WHITE
        override var constrainTo: UIComponent? = null
        override var recalculate = true

        private var originalAlpha: Int? = null

        init {
            isOverridden.onSetValue {
                recalculate = true
            }

            overriddenAlphaPercentage.onSetValue {
                recalculate = true
            }
        }

        override fun getColorImpl(component: UIComponent): Color {
            val originalColor = originalConstraint.getColorImpl(component)

            if (originalAlpha == null)
                originalAlpha = originalColor.alpha

            if (isOverridden.get())
                return originalColor.withAlpha((originalAlpha!! * overriddenAlphaPercentage.get()).roundToInt().coerceIn(0, 255))
            return originalColor
        }

        override fun visitImpl(visitor: ConstraintVisitor, type: ConstraintType) {
            // no-op
        }
    }
}
