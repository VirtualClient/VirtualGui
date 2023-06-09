package gg.virtualclient.virtualgui.constraints.resolution

import gg.virtualclient.virtualgui.UIComponent
import gg.virtualclient.virtualgui.constraints.ConstraintType
import gg.virtualclient.virtualgui.constraints.SuperConstraint

data class ResolverNode(
    val component: UIComponent,
    val constraint: SuperConstraint<*>,
    val constraintType: ConstraintType
)