package com.levin.uploadservice.okhttp

import java.io.IOException
import com.levin.uploadservice.network.BodyWriter
import okio.BufferedSink

/**
 * @author Aleksandar Gotev
 */

class OkHttpBodyWriter(private val sink: BufferedSink, listener: OnStreamWriteListener) :
    BodyWriter(listener) {
    @Throws(IOException::class)
    override fun internalWrite(bytes: ByteArray) {
        sink.write(bytes)
    }

    @Throws(IOException::class)
    override fun internalWrite(bytes: ByteArray, lengthToWriteFromStart: Int) {
        sink.write(bytes, 0, lengthToWriteFromStart)
    }

    @Throws(IOException::class)
    override fun flush() {
        sink.flush()
    }

    @Throws(IOException::class)
    override fun close() {
        sink.close()
    }
}
