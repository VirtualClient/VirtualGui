package com.example.examplemod

import gg.virtualclient.virtualgui.WindowScreen
import gg.virtualclient.virtualgui.components.*
import gg.virtualclient.virtualgui.constraints.*
import gg.virtualclient.virtualgui.dsl.*
import gg.virtualclient.virtualgui.effects.OutlineEffect
import net.kyori.adventure.text.Component
import java.awt.Color

class KtTestGui : WindowScreen(Component.empty()) {
    private val myTextBox = UIBlock(Color(0, 0, 0, 255))

    init {
        val container = UIContainer().constrain {
            x = RelativeConstraint(.25f)
            y = RelativeConstraint(.25f)
            width = RelativeConstraint(.5f)
            height = RelativeConstraint(.5f)
        } childOf window
        for (i in 50..500) {
            if (i % 15 != 0) continue
            UIBlock(Color.RED).constrain {
                x = CramSiblingConstraint(10 / 3f)
                y = CramSiblingConstraint(10f / 3f)
                width = 5.pixels()
                height = 5.pixels()
            } childOf container effect OutlineEffect(Color.BLUE, 1f);
        }
    }
}