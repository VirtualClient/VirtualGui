package gg.essential.elementa.font.data

import com.google.gson.JsonParser
import gg.virtualclient.virtualminecraft.util.ReleasedDynamicTexture
import java.io.InputStream

class Font(
    val fontInfo: FontInfo,
    private val atlas: InputStream
) {
    private lateinit var texture: ReleasedDynamicTexture

    fun getTexture(): ReleasedDynamicTexture {
        if (!::texture.isInitialized) {
            texture = ReleasedDynamicTexture.getTexture(atlas)
        }

        return texture
    }

    companion object {
        fun fromResource(path: String): Font {
            val json = this::class.java.getResourceAsStream("$path.json")!!
            val fontInfo = FontInfo.fromJson(JsonParser().parse(json.reader()).asJsonObject)

            return Font(fontInfo, this::class.java.getResourceAsStream("$path.png")!!)
        }
    }
}

