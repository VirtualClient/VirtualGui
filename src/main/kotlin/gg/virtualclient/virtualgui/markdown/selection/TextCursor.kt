package gg.virtualclient.virtualgui.markdown.selection

import gg.virtualclient.virtualgui.dsl.width
import gg.virtualclient.virtualgui.markdown.drawables.TextDrawable

/**
 * A simple class which points to a position in a TextDrawable.
 */
class TextCursor(target: TextDrawable, val offset: Int) : Cursor<TextDrawable>(target) {
    override val xBase = target.x +
        target.formattedText.substring(0, offset + target.style.numFormattingChars).width(target.scaleModifier)
    override val yBase = target.y

    override operator fun compareTo(other: Cursor<*>): Int {
        if (other !is TextCursor) {
            return target.y.compareTo(other.target.y).let {
                if (it == 0) target.x.compareTo(other.target.x) else it
            }
        }
        
        if (target == other.target)
            return offset.compareTo(other.offset)

        if (target.y == other.target.y)
            return target.x.compareTo(other.target.x)

        return target.y.compareTo(other.target.y)
    }
}
