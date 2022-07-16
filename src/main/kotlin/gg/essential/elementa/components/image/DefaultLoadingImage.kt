package gg.essential.elementa.components.image

import gg.essential.elementa.utils.drawTexture
import gg.virtualclient.virtualminecraft.VirtualMatrixStack
import gg.virtualclient.virtualminecraft.util.ReleasedDynamicTexture
import java.awt.Color
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

object DefaultLoadingImage : ImageProvider {
    private var loadingImage: BufferedImage? = ImageIO.read(this::class.java.getResourceAsStream("/loading.png"))
    private lateinit var loadingTexture: ReleasedDynamicTexture

    override fun drawImage(matrixStack: VirtualMatrixStack, x: Double, y: Double, width: Double, height: Double, color: Color) {
        if (!::loadingTexture.isInitialized) {
            loadingTexture = ReleasedDynamicTexture.getTexture(loadingImage!!)
            loadingImage = null
        }

        drawTexture(matrixStack, loadingTexture, color, x, y, width, height)
    }
}
