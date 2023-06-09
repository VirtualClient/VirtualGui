package gg.virtualclient.virtualgui.constraints

import gg.virtualclient.virtualgui.UIComponent

interface PaddingConstraint {

    fun getVerticalPadding(component: UIComponent): Float

    fun getHorizontalPadding(component: UIComponent) : Float
}