package io.github.wechaty.io.github.wechaty.utils

import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec
import java.io.*


class GenericCodec<T>(private val cls: Class<T>) : MessageCodec<T, T> {
    override fun encodeToWire(buffer: Buffer, s: T) {
        val bos = ByteArrayOutputStream()
        var out: ObjectOutput? = null
        try {
            out = ObjectOutputStream(bos)
            out.writeObject(s)
            out.flush()
            val yourBytes = bos.toByteArray()
            buffer.appendInt(yourBytes.size)
            buffer.appendBytes(yourBytes)
            out.close()
        } catch (e: IOException) {
        } finally {
            try {
                bos.close()
            } catch (ex: IOException) {
            }
        }
    }

    override fun decodeFromWire(pos: Int, buffer: Buffer): T? {
        // My custom message starting from this *position* of buffer
        var _pos = pos

        // Length of JSON
        val length: Int = buffer.getInt(_pos)

        // Jump 4 because getInt() == 4 bytes
        val yourBytes: ByteArray = buffer.getBytes(4.let { _pos += it; _pos }, length.let { _pos += it; _pos })
        val bis = ByteArrayInputStream(yourBytes)
        try {
            val ois = ObjectInputStream(bis)
            val msg = ois.readObject() as T
            ois.close()
            return msg
        } catch (e: IOException) {
            println("Listen failed " + e.message)
        } catch (e: ClassNotFoundException) {
            println("Listen failed " + e.message)
        } finally {
            try {
                bis.close()
            } catch (e: IOException) {
            }
        }
        return null
    }

    override fun transform(customMessage: T): T {
        // If a message is sent *locally* across the event bus.
        // This example sends message just as is
        return customMessage
    }

    override fun name(): String {
        // Each codec must have a unique name.
        // This is used to identify a codec when sending a message and for unregistering
        // codecs.
        return cls.simpleName + "Codec"
    }

    override fun systemCodecID(): Byte {
        // Always -1
        return -1
    }

}