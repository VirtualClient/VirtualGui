package gg.virtualclient.virtualgui.utils

@FunctionalInterface
interface TriConsumer <T, U, V> {
    fun accept(t: T, u: U, v: V)
}