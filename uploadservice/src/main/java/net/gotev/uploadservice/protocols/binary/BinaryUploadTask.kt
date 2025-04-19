package com.levin.uploadservice.protocols.binary

import com.levin.uploadservice.HttpUploadTask
import com.levin.uploadservice.extensions.addHeader
import com.levin.uploadservice.network.BodyWriter
import android.util.Base64

/**
 * Implements a binary file upload task.
 */
class BinaryUploadTask : HttpUploadTask() {
    private val file by lazy { params.files.first().handler }

    override val bodyLength: Long
        get() = file.size(context)

    override fun performInitialization() {
        with(httpParams.requestHeaders) {
            if (none { it.name.lowercase() == "content-type" }) {
                addHeader("Content-Type", file.contentType(context))
            }
        }
    }

    override fun onWriteRequestBody(bodyWriter: BodyWriter) {
        val keyBase64 = params.additionalParameters.getString("encryptionKey")
        val nonceBase64 = params.additionalParameters.getString("encryptionNonce")

        if (keyBase64 != null && nonceBase64 != null) {
            val keyBytes = Base64.decode(keyBase64.toByteArray(), Base64.DEFAULT)
            val nonceBytes = Base64.decode(nonceBase64.toByteArray(), Base64.DEFAULT)

            val secretKey = javax.crypto.spec.SecretKeySpec(keyBytes, "AES")
            val cipher = javax.crypto.Cipher.getInstance("AES/CTR/NoPadding")
            val ivSpec = javax.crypto.spec.IvParameterSpec(nonceBytes)
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey, ivSpec)

            val inputStream = java.io.BufferedInputStream(file.stream(context))
            val encryptedStream = javax.crypto.CipherInputStream(inputStream, cipher)

            bodyWriter.writeStream(encryptedStream)
        } else {
            // Fallback to normal upload if no encryption is provided
            bodyWriter.writeStream(file.stream(context))
        }
    }
}
