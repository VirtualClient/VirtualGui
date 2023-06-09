package com.example.examplemod;

import gg.virtualclient.virtualgui.UIComponent;
import gg.virtualclient.virtualgui.WindowScreen;
import gg.virtualclient.virtualgui.components.UIBlock;
import gg.virtualclient.virtualgui.constraints.CenterConstraint;
import gg.virtualclient.virtualgui.constraints.ChildBasedSizeConstraint;
import gg.virtualclient.virtualgui.constraints.PixelConstraint;
import gg.virtualclient.virtualgui.constraints.animation.AnimatingConstraints;
import gg.virtualclient.virtualgui.constraints.animation.Animations;
import gg.virtualclient.virtualgui.effects.ScissorEffect;
import net.kyori.adventure.text.Component;

public class JavaTestGui extends WindowScreen {
    UIComponent box = new UIBlock()
        .setX(new CenterConstraint())
        .setY(new PixelConstraint(10f))
        .setWidth(new PixelConstraint(10f))
        .setHeight(new PixelConstraint(36f))
        .setChildOf(getWindow())
        .enableEffect(new ScissorEffect());

    public JavaTestGui() {
        super(Component.empty());
        box.onMouseEnterRunnable(() -> {
            // Animate, set color, etc.
            AnimatingConstraints anim = box.makeAnimation();
            anim.setWidthAnimation(Animations.OUT_EXP, 0.5f, new ChildBasedSizeConstraint(2f));
            anim.onCompleteRunnable(() -> {
                // Trigger new animation or anything.
            });
            box.animateTo(anim);
        });
    }
}
