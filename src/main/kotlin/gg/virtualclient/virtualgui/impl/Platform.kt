package gg.essential.elementa.impl

import org.jetbrains.annotations.ApiStatus
import java.util.*

@ApiStatus.Internal
interface Platform {
    val mcVersion: Int

    var currentScreen: Any?

    val forceUnicodeFont: Boolean

    fun isAllowedInChat(char: Char): Boolean

    fun enableStencil()

    fun isCallingFromMinecraftThread(): Boolean

    fun scale(scaledWidth: Double, scaledHeight: Double)

    @ApiStatus.Internal
    companion object {
        internal val platform: Platform =
            ServiceLoader.load(Platform::class.java, Platform::class.java.classLoader).iterator().next()
    }
}