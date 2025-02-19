package gg.virtualclient.virtualgui.components.image

import gg.virtualclient.virtualminecraft.util.ReleasedDynamicTexture

interface CacheableImage {

    fun supply(image: CacheableImage)

    fun applyTexture(texture: ReleasedDynamicTexture?)

}