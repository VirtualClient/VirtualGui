@file:JvmName("PlatformUtil")
package gg.essential.elementa.dsl

import gg.essential.elementa.components.UIImage
import net.minecraft.client.MinecraftClient
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture
import javax.imageio.ImageIO

//import gg.essential.elementa.font.DefaultFonts
//import gg.essential.elementa.font.FontProvider
//import gg.essential.universal.wrappers.message.UTextComponent
//import net.minecraft.util.text.ITextComponent
//
//fun ITextComponent.width(textScale: Float = 1f, fontProvider: FontProvider = DefaultFonts.VANILLA_FONT_RENDERER) =
//    UTextComponent(this).formattedText.width(textScale, fontProvider)

fun UIImage.Companion.ofResource(identifier: Identifier): UIImage {
    return getUIImageFromIdentifier(identifier)
}

fun getUIImageFromIdentifier(identifier: Identifier): UIImage {
    return UIImage(CompletableFuture.supplyAsync {
        //#if MC>=11900
        ImageIO.read(MinecraftClient.getInstance().resourceManager.getResource(identifier).get().inputStream)
        //#else
        //$$ ImageIO.read(MinecraftClient.getInstance().resourceManager.getResource(identifier).inputStream)
        //#endif
    })
}
