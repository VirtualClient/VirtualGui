package gg.virtualclient.virtualgui.transitions

import gg.virtualclient.virtualgui.UIComponent
import gg.virtualclient.virtualgui.constraints.HeightConstraint
import gg.virtualclient.virtualgui.constraints.WidthConstraint
import gg.virtualclient.virtualgui.constraints.XConstraint
import gg.virtualclient.virtualgui.constraints.YConstraint
import gg.virtualclient.virtualgui.constraints.animation.AnimatingConstraints
import gg.virtualclient.virtualgui.constraints.animation.Animations
import gg.virtualclient.virtualgui.dsl.pixels
import gg.virtualclient.virtualgui.dsl.plus

/**
 * Shrinks a component towards its center.
 */
class ShrinkTransition @JvmOverloads constructor(
    private val time: Float = 1f,
    private val animationType: Animations = Animations.OUT_EXP,
    private val restoreConstraints: Boolean = false
) : Transition() {
    private val xConstraints = mutableMapOf<UIComponent, XConstraint>()
    private val yConstraints = mutableMapOf<UIComponent, YConstraint>()
    private val widthConstraints = mutableMapOf<UIComponent, WidthConstraint>()
    private val heightConstraints = mutableMapOf<UIComponent, HeightConstraint>()

    override fun beforeTransition(component: UIComponent) {
        xConstraints[component] = component.constraints.x
        yConstraints[component] = component.constraints.y
        widthConstraints[component] = component.constraints.width
        heightConstraints[component] = component.constraints.height
    }

    override fun doTransition(component: UIComponent, constraints: AnimatingConstraints) {
        constraints.apply {
            setXAnimation(animationType, time, xConstraints[component]!! + (component.getWidth() / 2f).pixels())
            setYAnimation(animationType, time, yConstraints[component]!! + (component.getHeight() / 2f).pixels())
            setWidthAnimation(animationType, time, 0.pixels())
            setHeightAnimation(animationType, time, 0.pixels())
        }
    }

    override fun afterTransition(component: UIComponent) {
        if (restoreConstraints) {
            component.setX(xConstraints[component]!!)
            component.setY(yConstraints[component]!!)
            component.setWidth(widthConstraints[component]!!)
            component.setHeight(heightConstraints[component]!!)
        }
        xConstraints.remove(component)
        yConstraints.remove(component)
        widthConstraints.remove(component)
        heightConstraints.remove(component)
    }
}
