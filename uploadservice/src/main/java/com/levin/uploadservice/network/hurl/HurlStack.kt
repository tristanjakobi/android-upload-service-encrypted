package com.levin.uploadservice.network.hurl

import com.levin.uploadservice.UploadServiceConfig
import com.levin.uploadservice.network.HttpRequest
import com.levin.uploadservice.network.HttpStack
import java.io.IOException

class HurlStack @JvmOverloads constructor(
    private val userAgent: String = UploadServiceConfig.defaultUserAgent,
    private val followRedirects: Boolean = true,
    private val useCaches: Boolean = false,
    private val connectTimeoutMillis: Int = 15000,
    private val readTimeoutMillis: Int = 30000
) : HttpStack {

    @Throws(IOException::class)
    override fun newRequest(uploadId: String, method: String, url: String): HttpRequest {
        return HurlStackRequest(
            userAgent, uploadId, method, url, followRedirects, useCaches,
            connectTimeoutMillis, readTimeoutMillis
        )
    }
}
