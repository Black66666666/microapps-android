package com.microapps.matchchoice

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.Base64

data class Invite(val title: String, val options: List<String>, val creator: List<String>)

object InviteCodec {
    private const val VERSION = 1
    private const val MAX_ITEMS = 100

    fun encode(invite: Invite): String {
        val bytes = ByteArrayOutputStream().use { buffer ->
            DataOutputStream(buffer).use { output ->
                output.writeInt(VERSION)
                output.writeUTF(invite.title)
                output.writeStrings(invite.options)
                output.writeStrings(invite.creator)
            }
            buffer.toByteArray()
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    fun decode(code: String): Invite? = runCatching {
        val bytes = Base64.getUrlDecoder().decode(code.trim())
        DataInputStream(ByteArrayInputStream(bytes)).use { input ->
            require(input.readInt() == VERSION)
            val invite = Invite(input.readUTF(), input.readStrings(), input.readStrings())
            require(input.available() == 0)
            invite
        }
    }.getOrNull()

    private fun DataOutputStream.writeStrings(values: List<String>) {
        require(values.size <= MAX_ITEMS)
        writeInt(values.size)
        values.forEach(::writeUTF)
    }

    private fun DataInputStream.readStrings(): List<String> {
        val count = readInt()
        require(count in 0..MAX_ITEMS)
        return List(count) { readUTF() }
    }
}

fun matchingChoices(creator: List<String>, guest: Set<String>): List<String> = creator.filter { it in guest }
