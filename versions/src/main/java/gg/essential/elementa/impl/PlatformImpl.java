package gg.essential.elementa.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import gg.virtualclient.virtualminecraft.VirtualMinecraft;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
@SuppressWarnings("unused") // instantiated via reflection from Platform.Companion
public class PlatformImpl implements Platform {

    @Override
    public int getMcVersion() {
        //#if MC>=11903
        //$$ return 11903;
        //#elseif MC==11900
        return 11900;
        //#elseif MC==11802
        //$$ return 11802;
        //#elseif MC==11701
        //$$ return 11701;
        //#elseif MC==11605
        //$$ return 11605;
        //#elseif MC==11502
        //$$ return 11502;
        //#elseif MC==11202
        //$$  return 11202;
        //#elseif MC==10809
        //$$ return 10809;
        //#endif
    }

    @Nullable
    @Override
    public Object getCurrentScreen() {
        return MinecraftClient.getInstance().currentScreen;
    }

    @Override
    public void setCurrentScreen(@Nullable Object screen) {
        MinecraftClient.getInstance().setScreen((Screen) screen);
    }

    @Override
    public boolean isAllowedInChat(char c) {
        return SharedConstants.isValidChar(c);
    }

    @Override
    public void enableStencil() {
        //#if MC<11500
        //$$ Framebuffer framebuffer = Minecraft.getMinecraft().getFramebuffer();
        //$$ if (!framebuffer.isStencilEnabled()) {
        //$$     framebuffer.enableStencil();
        //$$ }
        //#endif
    }

    @Override
    public boolean isCallingFromMinecraftThread() {
        //#if MC>=11400
        return MinecraftClient.getInstance().isOnThread();
        //#else
        //$$ return Minecraft.getMinecraft().isCallingFromMinecraftThread();
        //#endif
    }

    @Override
    public boolean getForceUnicodeFont() {
        //#if MC>=11900
        return MinecraftClient.getInstance().options.getForceUnicodeFont().getValue();
        //#else
        //$$ return MinecraftClient.getInstance().options.forceUnicodeFont;
        //#endif
    }

    @Override
    public void scale(double scaledWidth, double scaledHeight) {
        //#if MC>=11701
        RenderSystem.clear(256, VirtualMinecraft.INSTANCE.isRunningOnMac());
        //#if MC>=11903
        //$$ Matrix4f matrix4f = new Matrix4f().setOrtho(0.0f, (float) scaledWidth, 0.0f, (float) scaledHeight, 1000.0f, 3000.0f);
        //#else
        Matrix4f matrix4f = Matrix4f.projectionMatrix(0.0f, (float) scaledWidth, 0.0f, (float) scaledHeight, 1000.0f, 3000.0f);
        //#endif

        RenderSystem.setProjectionMatrix(matrix4f);
        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.loadIdentity();
        matrixStack.translate(0.0, 0.0, -2000.0);
        RenderSystem.applyModelViewMatrix();
        //#else
        //$$ RenderSystem.clear(256, VirtualMinecraft.INSTANCE.isRunningOnMac());
        //$$ RenderSystem.matrixMode(5889);
        //$$ RenderSystem.loadIdentity();
        //$$ RenderSystem.ortho(0.0, scaledWidth, scaledHeight, 0.0, 1000.0, 3000.0);
        //$$ RenderSystem.matrixMode(5888);
        //$$ RenderSystem.loadIdentity();
        //$$ RenderSystem.translatef(0.0F, 0.0F, -2000.0F);
        //#endif
    }
}
