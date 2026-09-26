package com.microapps.whobringswhat

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.Base64

data class BringItem(val name: String, val owner: String = "")
data class BringState(val eventName: String, val items: List<BringItem>)

object BringStateCodec {
    private const val VERSION = 1
    private const val MAX_ITEMS = 200

    fun encode(state: BringState): String {
        val bytes = ByteArrayOutputStream().use { buffer ->
            DataOutputStream(buffer).use { output ->
                output.writeInt(VERSION)
                output.writeUTF(state.eventName)
                require(state.items.size <= MAX_ITEMS)
                output.writeInt(state.items.size)
                state.items.forEach { output.writeUTF(it.name); output.writeUTF(it.owner) }
            }
            buffer.toByteArray()
        }
        return Base64.getEncoder().encodeToString(bytes)
    }

    fun decode(value: String): BringState? = runCatching {
        if (value.isBlank()) return null
        DataInputStream(ByteArrayInputStream(Base64.getDecoder().decode(value))).use { input ->
            require(input.readInt() == VERSION)
            val event = input.readUTF()
            val count = input.readInt()
            require(count in 0..MAX_ITEMS)
            val items = List(count) { BringItem(input.readUTF(), input.readUTF()) }
            require(input.available() == 0)
            BringState(event, items)
        }
    }.getOrNull()
}

fun formatShareText(state: BringState): String = buildString {
    appendLine("${state.eventName} — кто что приносит")
    state.items.forEach { appendLine("• ${it.name}: ${it.owner.ifBlank { "свободно" }}") }
    append("Создай свой список в WhoBringsWhat")
}
