package gg.virtualclient.virtualgui.utils

import gg.virtualclient.virtualgui.dsl.width
import gg.virtualclient.virtualgui.font.DefaultFonts
import gg.virtualclient.virtualgui.font.FontProvider
import gg.virtualclient.virtualminecraft.VirtualTextRenderer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent

fun getStringSplitToWidthTruncated(
    text: String,
    maxLineWidth: Float,
    textScale: Float,
    maxLines: Int,
    ensureSpaceAtEndOfLines: Boolean = true,
    processColorCodes: Boolean = true,
    fontProvider: FontProvider = DefaultFonts.VANILLA_FONT_RENDERER,
    trimmedTextSuffix: String = "..."
): List<String> {
    val lines = getStringSplitToWidth(text, maxLineWidth, textScale, ensureSpaceAtEndOfLines, processColorCodes,fontProvider)
    if (lines.size <= maxLines)
        return lines

    val suffixWidth = trimmedTextSuffix.width(textScale,fontProvider)

    return lines.subList(0, maxLines).mapIndexed { index, contents ->
        var length = contents.lastIndex
        if (index == maxLines - 1 && length > 0) {
            while (length > 0 && contents.substring(0, length).width(textScale,fontProvider) + suffixWidth > maxLineWidth * textScale)
                length--
            contents.substring(0, length) + trimmedTextSuffix
        } else contents
    }
}

fun getComponentSplitToWidth(
    text: Component,
    maxLineWidth: Float,
    textScale: Float,
    ensureSpaceAtEndOfLines: Boolean = true,
    fontProvider: FontProvider = DefaultFonts.VANILLA_FONT_RENDERER
): List<Component> {
    val allComponents = mutableListOf(text)
    fun addAllChildren(component: Component) {
        component.children().forEach {
            allComponents.add(it)
            if(it.children().isNotEmpty()) {
                addAllChildren(it)
            }
        }
    }
    addAllChildren(text)

    val maxWidth = maxLineWidth - if (ensureSpaceAtEndOfLines) ' '.width(textScale) else 0f

    var currentComponent: TextComponent.Builder = Component.text()
    val components = mutableListOf<Component>()

    var currentWidth = 0.0F


    allComponents.forEach {
        val width = fontProvider.getStringWidth(it, 10f) * textScale
        if(currentWidth + width < maxWidth) {
            currentWidth+=width
            currentComponent.append(it)
            return@forEach
        }

        val content: String = if(it is TextComponent) {
            it.content()
        } else if(it is TranslatableComponent) {
            //TODO: Implement
            ""
        } else {
            println("Warning: Tried to get width of unsupported text-component ${it.javaClass.simpleName}. Ignoring")
            return@forEach
        }

        var builder = StringBuilder()

        content.toCharArray().forEach { char ->
            val charWidth = fontProvider.getStringWidth(Component.text(char, it.style()), 10f) * textScale

            if(charWidth + currentWidth > maxWidth) {
                currentComponent.append(Component.text(builder.toString(), it.style()))
                components.add(currentComponent.build())
                currentComponent = Component.text()

                builder = StringBuilder()
            }

            builder.append(char)
        }
        val toString = builder.toString()
        if(toString.isNotEmpty()) {
            currentComponent.append(Component.text(builder.toString(), it.style()))
        }
    }

    val build = currentComponent.build()
    if(build.content().isNotEmpty() || build.children().isNotEmpty()) {
        components.add(currentComponent.build())
    }

    return components
}

fun getStringSplitToWidth(
    text: String,
    maxLineWidth: Float,
    textScale: Float,
    ensureSpaceAtEndOfLines: Boolean = true,
    processColorCodes: Boolean = true,
    fontProvider: FontProvider = DefaultFonts.VANILLA_FONT_RENDERER
): List<String> {
    val spaceWidth = ' '.width(textScale)
    val maxLineWidthSpace = maxLineWidth - if (ensureSpaceAtEndOfLines) spaceWidth else 0f
    val lineList = mutableListOf<String>()
    val currLine = StringBuilder()
    var textPos = 0
    var currChatColor: LegacyColorCode? = null
    var currChatFormatting: LegacyColorCode? = null

    fun pushLine() {
        lineList.add(currLine.toString())
        currLine.clear()
        if (processColorCodes) {
            currChatColor?.also { currLine.append("§${it.char}") }
            currChatFormatting?.also { currLine.append("§${it.char}") }
        }
    }

    while (textPos < text.length) {
        val builder = StringBuilder()

        while (textPos < text.length && text[textPos].let { it != ' ' && it != '\n'}) {
            val ch = text[textPos]
            if (processColorCodes && (ch == '§') && textPos + 1 < text.length) {
                val colorCh = text[textPos + 1]
                val nextColor = LegacyColorCode.values().firstOrNull { it.char == colorCh }
                if (nextColor != null) {
                    builder.append('§')
                    builder.append(colorCh)

                    if (nextColor.isFormat) {
                        currChatFormatting = nextColor
                    } else {
                        currChatColor = nextColor
                    }

                    textPos += 2
                    continue
                }
            }

            builder.append(ch)
            textPos++
        }

        val newline = textPos < text.length && text[textPos] == '\n'
        val word = builder.toString()
        val wordWidth = word.width(textScale, fontProvider)

        if (processColorCodes && newline) {
            currChatColor = null
            currChatFormatting = null
        }

        if ((currLine.toString() + word).width(textScale, fontProvider) > maxLineWidthSpace) {
            if (wordWidth > maxLineWidthSpace) {
                // Split up the word into it's own lines
                if (currLine.toString().width(textScale, fontProvider) > 0)
                    pushLine()

                for (char in word.toCharArray()) {
                    if ((currLine.toString() + char).width(textScale, fontProvider) > maxLineWidthSpace)
                        pushLine()
                    currLine.append(char)
                }
            } else {
                pushLine()
                currLine.append(word)
            }

            // Check if we have a space, and if so, append it to the new line
            if (textPos < text.length) {
                if (!newline) {
                    if (currLine.toString().width(textScale, fontProvider) + spaceWidth > maxLineWidthSpace)
                        pushLine()
                    currLine.append(' ')
                    textPos++
                } else {
                    pushLine()
                    textPos++
                }
            }
        } else {
            currLine.append(word)

            // Check if we have a space, and if so, append it to a line
            if (!newline && textPos < text.length) {
                textPos++
                currLine.append(' ')
            } else if (newline) {
                pushLine()
                textPos++
            }
        }
    }

    lineList.add(currLine.toString())

    return lineList
}

fun sizeStringToWidth(string: String, width: Float): Int {
    val i = string.length
    var j = 0f
    var k = 0
    var l = -1

    var flag = false
    while (k < i) {
        val c0: Char = string[k]

        when (c0) {
            '\n' -> k--
            ' ' -> {
                l = k
                j += VirtualTextRenderer.getInstance().getWidth(c0.toString())

                if (flag) j++
            }
            '\u00a7' -> if (k < i - 1) {
                k++
                val c1 = string[k]
                if (c1.code != 108 && c1.code != 76) {
                    if (c1.code == 114 || c1.code == 82 || isFormatColor(c1.code)) {
                        flag = false
                    }
                } else {
                    flag = true
                }
            }
            else -> {
                j += VirtualTextRenderer.getInstance().getWidth(c0.toString())

                if (flag) j++
            }
        }

        if (c0.code == 10) {
            k++
            l = k
            break
        }

        if (j > width) break

        k++
    }

    return if (k != i && l != -1 && l < k) l else k
}

fun isFormatColor(char: Int) = char in 48..57 || char in 97..102 || char in 65..70
