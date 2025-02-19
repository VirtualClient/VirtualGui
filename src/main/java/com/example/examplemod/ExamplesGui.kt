package com.example.examplemod

import gg.virtualclient.virtualgui.WindowScreen
import gg.virtualclient.virtualgui.components.*
import gg.virtualclient.virtualgui.constraints.*
import gg.virtualclient.virtualgui.constraints.animation.Animations
import gg.virtualclient.virtualgui.dsl.*
import gg.virtualclient.virtualgui.impl.Platform.Companion.platform
import gg.virtualclient.virtualminecraft.VirtualScreen
import net.kyori.adventure.text.Component
import java.awt.Color

/**
 * List of buttons to open a specific example gui.
 * See ExampleGui (singular) for a well-commented example gui.
 */
class ExamplesGui : WindowScreen(Component.empty()) {
    private val container by ScrollComponent().constrain {
        y = 3.pixels()
        width = 100.percent()
        height = 100.percent() - (3 * 2).pixels()
    } childOf window

    init {
        for ((name, action) in examples) {
            val button = UIBlock().constrain {
                x = CenterConstraint()
                y = SiblingConstraint(padding = 3f)
                width = 200.pixels()
                height = 20.pixels()
                color = Color(255, 255, 255, 102).toConstraint()
            }.onMouseEnter {
                animate {
                    setColorAnimation(Animations.OUT_EXP, 0.5f, Color(255, 255, 255, 150).toConstraint())
                }
            }.onMouseLeave {
                animate {
                    setColorAnimation(Animations.OUT_EXP, 0.5f, Color(255, 255, 255, 102).toConstraint())
                }
            }.onMouseClick {
                try {
                    platform.currentScreen = action()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } childOf container

            UIText(name).constrain {
                x = CenterConstraint()
                y = CenterConstraint()
            } childOf button
        }
    }

    companion object {
        val examples = mutableMapOf<String, () -> VirtualScreen>(
            "ExampleGui" to ::ExampleGui,
            "ComponentsGui" to ::ComponentsGui,
        )
    }
}
