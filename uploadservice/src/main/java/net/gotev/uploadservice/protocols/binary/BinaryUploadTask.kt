package net.gotev.uploadservice.protocols.binary

import net.gotev.uploadservice.HttpUploadTask
import net.gotev.uploadservice.extensions.addHeader
import net.gotev.uploadservice.network.BodyWriter

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
        val keyBase64 = params.taskParameters["encryptionKey"]
        val nonceBase64 = params.taskParameters["encryptionNonce"]

        if (keyBase64 != null && nonceBase64 != null) {
            val keyBytes = android.util.Base64.decode(keyBase64, android.util.Base64.DEFAULT)
            val nonceBytes = android.util.Base64.decode(nonceBase64, android.util.Base64.DEFAULT)

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
