package com.levin.uploadservice.network.hurl

import java.io.IOException
import java.io.OutputStream
import com.levin.uploadservice.network.BodyWriter

class HurlBodyWriter(private val stream: OutputStream, listener: OnStreamWriteListener) :
    BodyWriter(listener) {
    @Throws(IOException::class)
    override fun internalWrite(bytes: ByteArray) {
        stream.write(bytes)
    }

    @Throws(IOException::class)
    override fun internalWrite(bytes: ByteArray, lengthToWriteFromStart: Int) {
        stream.write(bytes, 0, lengthToWriteFromStart)
    }

    @Throws(IOException::class)
    override fun flush() {
        stream.flush()
    }

    @Throws(IOException::class)
    override fun close() {
        stream.close()
    }
}
