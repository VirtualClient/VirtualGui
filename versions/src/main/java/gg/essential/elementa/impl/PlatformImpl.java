package gg.essential.elementa.impl;

import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
@SuppressWarnings("unused") // instantiated via reflection from Platform.Companion
public class PlatformImpl implements Platform {

    @Override
    public int getMcVersion() {
        //#if MC==11900
        return 11900;
        //#elseif MC==11802
        //$$ return 11802;
        //#elseif MC==11701
        //$$ return 11701;
        //#elseif MC==11604
        //$$ return 11604;
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
}
