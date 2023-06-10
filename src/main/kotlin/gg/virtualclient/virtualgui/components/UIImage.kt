package gg.virtualclient.virtualgui.components

import gg.virtualclient.virtualgui.UIComponent
import gg.virtualclient.virtualgui.svg.SVGParser
import gg.virtualclient.virtualgui.components.image.*
import gg.virtualclient.virtualgui.utils.ResourceCache
import gg.virtualclient.virtualgui.utils.drawTexture
import gg.virtualclient.virtualminecraft.VirtualMatrixStack
import gg.virtualclient.virtualminecraft.util.ReleasedDynamicTexture
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentLinkedQueue
import javax.imageio.ImageIO

/**
 * Component for drawing arbitrary images from [BufferedImage].
 *
 * There are companion functions available to get [UIImage]s from other sources,
 * such as URLs: [Companion.ofURL], [Companion.ofFile] and [Companion.ofResource].
 */
open class UIImage @JvmOverloads constructor(
    private val imageFuture: CompletableFuture<BufferedImage>,
    private val loadingImage: ImageProvider = DefaultLoadingImage,
    private val failureImage: ImageProvider = DefaultFailureImage,
) : UIComponent(), ImageProvider, CacheableImage {
    private var texture: ReleasedDynamicTexture? = null

    private val waiting = ConcurrentLinkedQueue<CacheableImage>()
    var imageWidth = 1f
    var imageHeight = 1f
    var destroy = true
    val isLoaded: Boolean
        get() = texture != null
    var textureMinFilter = TextureScalingMode.NEAREST
    var textureMagFilter = TextureScalingMode.NEAREST

    init {
        imageFuture.exceptionally {
            it.printStackTrace()
            return@exceptionally null
        }.thenAcceptAsync {
            if (it == null) {
                destroy = false
                return@thenAcceptAsync
            }
            imageWidth = it.width.toFloat()
            imageHeight = it.height.toFloat()
            imageFuture.obtrudeValue(null)

            // In versions before 1.15, we make the bufferedImage.getRGB call without the upload in the
            // constructor since that takes most of the CPU time and we upload the actual texture during the
            // first call to uploadTexture or getGlTextureId
            // Same for 1.15+ actually, except that it is not getRGB but serialization to byte[] (so we can re-parse it
            // as a NativeImage) which is slow.
            val texture = ReleasedDynamicTexture.getTexture(it)
            Window.enqueueRenderOperation {
                texture.uploadTexture()
                this.texture = texture
                while (waiting.isEmpty().not())
                    waiting.poll().applyTexture(texture)
            }
        }
    }

    override fun drawImage(matrixStack: VirtualMatrixStack, x: Double, y: Double, width: Double, height: Double, color: Color) {
        when {
            texture != null -> drawTexture(matrixStack, texture!!, color, x, y, width, height, textureMinFilter.glMode, textureMagFilter.glMode)
            imageFuture.isCompletedExceptionally -> failureImage.drawImage(matrixStack, x, y, width, height, color)
            else -> loadingImage.drawImage(matrixStack, x, y, width, height, color)
        }
    }

    override fun draw(matrixStack: VirtualMatrixStack) {
        beforeDraw(matrixStack)

        val x = this.getLeft().toDouble()
        val y = this.getTop().toDouble()
        val width = this.getWidth().toDouble()
        val height = this.getHeight().toDouble()
        val color = this.getColor()

        if (color.alpha == 0) {
            return super.draw(matrixStack)
        }

        drawImage(matrixStack, x, y, width, height, color)

        super.draw(matrixStack)
    }

    override fun supply(image: CacheableImage) {
        if (texture != null) {
            image.applyTexture(texture)
            return
        }
        waiting.add(image)
    }

    override fun applyTexture(texture: ReleasedDynamicTexture?) {
        this.texture = texture
        while (waiting.isEmpty().not())
            waiting.poll().applyTexture(texture)
    }

    enum class TextureScalingMode(internal val glMode: Int) {
        NEAREST(GL11.GL_NEAREST),
        LINEAR(GL11.GL_LINEAR),
        NEAREST_MIPMAP_NEAREST(GL11.GL_NEAREST_MIPMAP_NEAREST),
        LINEAR_MIPMAP_NEAREST(GL11.GL_LINEAR_MIPMAP_NEAREST),
        NEAREST_MIPMAP_LINEAR(GL11.GL_NEAREST_MIPMAP_LINEAR),
        LINEAR_MIPMAP_LINEAR(GL11.GL_LINEAR_MIPMAP_LINEAR)
    }

    companion object {

        val defaultResourceCache = ResourceCache(50)

        @JvmStatic
        fun ofFile(file: File): UIImage {
            return UIImage(CompletableFuture.supplyAsync { ImageIO.read(file) })
        }

        @JvmStatic
        fun ofURL(url: URL): UIImage {
            return UIImage(CompletableFuture.supplyAsync { get(url) })
        }

        @JvmStatic
        fun ofURL(url: URL, cache: ImageCache): UIImage {
            return UIImage(CompletableFuture.supplyAsync {
                return@supplyAsync cache[url] ?: get(url).also {
                    cache[url] = it
                }
            })
        }

        @JvmStatic
        fun ofResource(path: String): UIImage {
            return UIImage(CompletableFuture.supplyAsync {
                ImageIO.read(this::class.java.getResourceAsStream(path))
            })
        }

        @JvmStatic
        fun ofResourceCached(path: String): UIImage {
            return ofResourceCached(path, defaultResourceCache)
        }

        @JvmStatic
        fun ofResourceCached(path: String, resourceCache: ResourceCache): UIImage {
            return resourceCache.getUIImage(path) as UIImage
        }

        @JvmStatic
        fun get(url: URL): BufferedImage {
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.useCaches = true
            connection.addRequestProperty("User-Agent", "Mozilla/4.76 (Elementa)")
            connection.doOutput = true

            return ImageIO.read(connection.inputStream)
        }
    }
}
