package gg.virtualclient.virtualgui.constraints

import gg.virtualclient.virtualgui.UIComponent
import gg.virtualclient.virtualgui.components.UIText
import gg.virtualclient.virtualgui.constraints.resolution.ConstraintVisitor
import java.lang.UnsupportedOperationException

/**
 * For size:
 * Sets the width/height to be [value] multiple of its own height/width respectively.
 *
 * For position:
 * Sets the x/y position to be [value] multiple of its own y/x position respectively.
 */
class TextAspectConstraint : WidthConstraint, HeightConstraint {
    override var cachedValue = 0f
    override var recalculate = true
    override var constrainTo: UIComponent? = null

    override fun getWidthImpl(component: UIComponent): Float {
        val text = (component as? UIText)?.getText() ?: throw IllegalStateException("TextAspectConstraint can only be used in UIText components")
        return component.getFontProvider().getStringWidth(text, 10f) * component.getHeight() / 9
    }

    override fun getHeightImpl(component: UIComponent): Float {
        val text = (component as? UIText)?.getText() ?: throw IllegalStateException("TextAspectConstraint can only be used in UIText components")
        return 9 * component.getWidth() / component.getFontProvider().getStringWidth(text, 10f)
    }

    override fun to(component: UIComponent) = apply {
        throw UnsupportedOperationException("Constraint.to(UIComponent) is not available in this context!")
    }

    override fun visitImpl(visitor: ConstraintVisitor, type: ConstraintType) {
        when (type) {
            ConstraintType.WIDTH -> visitor.visitSelf(ConstraintType.HEIGHT)
            ConstraintType.HEIGHT -> visitor.visitSelf(ConstraintType.WIDTH)
            else -> throw IllegalArgumentException(type.prettyName)
        }
    }
}
