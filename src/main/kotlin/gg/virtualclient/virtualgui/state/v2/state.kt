package gg.virtualclient.virtualgui.state.v2

/**
 * Holds strong references to listeners to prevent them from being garbage collected.
 */
interface ReferenceHolder {
  fun holdOnto(listener: Any): () -> Unit

  object Weak : ReferenceHolder {
    override fun holdOnto(listener: Any): () -> Unit = {}
  }
}
