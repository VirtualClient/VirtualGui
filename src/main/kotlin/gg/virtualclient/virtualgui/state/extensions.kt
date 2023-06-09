package gg.virtualclient.virtualgui.state

import gg.virtualclient.virtualgui.constraints.ConstantColorConstraint
import gg.virtualclient.virtualgui.constraints.PixelConstraint
import gg.virtualclient.virtualgui.constraints.RelativeConstraint
import java.awt.Color

fun State<Float>.pixels(
    alignOpposite: Boolean = false,
    alignOutside: Boolean = false
) = PixelConstraint(0f, alignOpposite, alignOutside).bindValue(this)
@JvmName("pixelsNumber")
fun State<Number>.pixels(alignOpposite: Boolean = false, alignOutside: Boolean) =
    PixelConstraint(0f, alignOpposite, alignOutside).bindValue(this.map { it.toFloat() })
val State<Number>.pixels: PixelConstraint
    get() = pixels(alignOpposite = false, alignOutside = false)

fun State<Float>.percent() = RelativeConstraint().bindValue(this)
@JvmName("percentNumber")
fun State<Number>.percent() =
    RelativeConstraint().bindValue(this.map { it.toFloat() })
val State<Number>.percent: RelativeConstraint
    get() = percent()

fun State<Color>.toConstraint() = ConstantColorConstraint(this.get()).bindColor(this)
val State<Color>.constraint: ConstantColorConstraint
    get() = toConstraint()
