package gg.virtualclient.virtualgui.components

import gg.virtualclient.virtualgui.components.inspector.ArrowComponent
import gg.virtualclient.virtualgui.constraints.SiblingConstraint
import gg.virtualclient.virtualgui.dsl.constrain
import gg.virtualclient.virtualgui.dsl.pixels
import gg.virtualclient.virtualgui.markdown.drawables.Drawable
import gg.virtualclient.virtualgui.markdown.drawables.TextDrawable

internal class MarkdownNode(private val targetDrawable: Drawable) : TreeNode() {
    private val componentClassName = targetDrawable.javaClass.simpleName.ifEmpty { "UnknownType" }
    private val componentDisplayName =
        componentClassName + if (targetDrawable is TextDrawable) " \"${targetDrawable.formattedText}\"" else ""

    private val component = UIText(componentDisplayName).constrain {
        x = SiblingConstraint()
        y = 2.pixels
    }

    init {
        targetDrawable.children.forEach {
            addChild(MarkdownNode(it))
        }
    }
    override fun getArrowComponent() = ArrowComponent(targetDrawable.children.isEmpty())

    override fun getPrimaryComponent() = component
}
