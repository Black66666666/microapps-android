package com.microapps.buytomorrow

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.Base64

data class Wish(val name: String, val amount: Double, val createdAt: Long, val waitHours: Int, val status: String = "waiting")

object WishCodec {
    private const val VERSION = 1
    private const val MAX_ITEMS = 1000

    fun encode(wishes: List<Wish>): String {
        val bytes = ByteArrayOutputStream().use { buffer ->
            DataOutputStream(buffer).use { output ->
                output.writeInt(VERSION)
                require(wishes.size <= MAX_ITEMS)
                output.writeInt(wishes.size)
                wishes.forEach { wish ->
                    output.writeUTF(wish.name)
                    output.writeDouble(wish.amount)
                    output.writeLong(wish.createdAt)
                    output.writeInt(wish.waitHours)
                    output.writeUTF(wish.status)
                }
            }
            buffer.toByteArray()
        }
        return Base64.getEncoder().encodeToString(bytes)
    }

    fun decode(value: String): List<Wish> = runCatching {
        if (value.isBlank()) return emptyList()
        DataInputStream(ByteArrayInputStream(Base64.getDecoder().decode(value))).use { input ->
            require(input.readInt() == VERSION)
            val count = input.readInt()
            require(count in 0..MAX_ITEMS)
            val wishes = List(count) { Wish(input.readUTF(), input.readDouble(), input.readLong(), input.readInt(), input.readUTF()) }
            require(input.available() == 0)
            wishes
        }
    }.getOrDefault(emptyList())
}

fun savedAmount(wishes: List<Wish>): Double = wishes.filter { it.status == "skipped" }.sumOf { it.amount }

fun updateWishStatus(wishes: List<Wish>, createdAt: Long, status: String): List<Wish> =
    wishes.map { if (it.createdAt == createdAt) it.copy(status = status) else it }
