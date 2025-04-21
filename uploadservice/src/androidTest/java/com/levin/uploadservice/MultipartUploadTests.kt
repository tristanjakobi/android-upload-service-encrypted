package com.levin.uploadservice

import com.levin.uploadservice.protocols.multipart.MultipartUploadRequest
import com.levin.uploadservice.testcore.UploadServiceTestSuite
import com.levin.uploadservice.testcore.assertBodySizeIsLowerOrEqualThanDeclaredContentLength
import com.levin.uploadservice.testcore.assertContentTypeIsMultipartFormData
import com.levin.uploadservice.testcore.assertDeclaredContentLengthMatchesPostBodySize
import com.levin.uploadservice.testcore.assertEmptyBodyAndHttpCodeIs
import com.levin.uploadservice.testcore.assertFile
import com.levin.uploadservice.testcore.assertHeader
import com.levin.uploadservice.testcore.assertHttpMethodIs
import com.levin.uploadservice.testcore.assertParameter
import com.levin.uploadservice.testcore.baseUrl
import com.levin.uploadservice.testcore.createTestFile
import com.levin.uploadservice.testcore.getBlockingResponse
import com.levin.uploadservice.testcore.multipartBodyParts
import com.levin.uploadservice.testcore.readFile
import com.levin.uploadservice.testcore.requireCancelledByUser
import com.levin.uploadservice.testcore.requireOtherError
import com.levin.uploadservice.testcore.requireServerError
import com.levin.uploadservice.testcore.requireSuccessful
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.mockwebserver.SocketPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit

class MultipartUploadTests : UploadServiceTestSuite() {

    private fun createMultipartUploadRequest() =
        MultipartUploadRequest(appContext, mockWebServer.baseUrl)
            .setBearerAuth("bearerToken")
            .setUsesFixedLengthStreamingMode(true)
            .addHeader("User-Agent", "SomeUserAgent")
            .addParameter("privacy", "1")
            .addParameter("nsfw", "false")
            .addParameter("name", "myfilename")
            .addParameter("commentsEnabled", "true")
            .addParameter("downloadEnabled", "true")
            .addParameter("waitTranscoding", "true")
            .addParameter("channelId", "123456")
            .addFileToUpload(appContext.createTestFile("testFile"), "videofile")
            .addFileToUpload(
                appContext.createTestFile("testFile2"),
                "videofile2",
                contentType = "video/mp4"
            )
            .setMaxRetries(0)

    private fun RecordedRequest.verifyMultipartUploadRequestHeadersAndBody() {
        assertHttpMethodIs("POST")
        assertDeclaredContentLengthMatchesPostBodySize()
        assertContentTypeIsMultipartFormData()
        assertHeader("Authorization", "Bearer bearerToken")
        assertHeader("User-Agent", "SomeUserAgent")

        multipartBodyParts.apply {
            assertEquals("number of parts is wrong", 9, size)
            assertParameter("privacy", "1")
            assertParameter("nsfw", "false")
            assertParameter("name", "myfilename")
            assertParameter("commentsEnabled", "true")
            assertParameter("downloadEnabled", "true")
            assertParameter("waitTranscoding", "true")
            assertParameter("channelId", "123456")
            assertFile(
                parameterName = "videofile",
                fileContent = appContext.readFile("testFile"),
                filename = "testFile",
                contentType = "application/octet-stream"
            )
            assertFile(
                parameterName = "videofile2",
                fileContent = appContext.readFile("testFile2"),
                filename = "testFile2",
                contentType = "video/mp4"
            )
        }
    }

    @Test
    fun successfulMultipartUpload() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val uploadRequest = createMultipartUploadRequest()

        val response = uploadRequest.getBlockingResponse(appContext).requireSuccessful()

        response.assertEmptyBodyAndHttpCodeIs(200)

        mockWebServer.takeRequest().verifyMultipartUploadRequestHeadersAndBody()
    }

    @Test
    fun successfulMultipartUploadAfterOneRetry() {
        mockWebServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_DURING_REQUEST_BODY))
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val uploadRequest = createMultipartUploadRequest()
            .setMaxRetries(1)

        val response = uploadRequest.getBlockingResponse(appContext).requireSuccessful()

        response.assertEmptyBodyAndHttpCodeIs(200)

        mockWebServer.takeRequest() // discard the first request being made
        mockWebServer.takeRequest().verifyMultipartUploadRequestHeadersAndBody()
    }

    @Test
    fun serverErrorMultipartUpload() {
        mockWebServer.enqueue(MockResponse().setResponseCode(400))

        val uploadRequest = createMultipartUploadRequest()

        val response = uploadRequest.getBlockingResponse(appContext).requireServerError()

        response.assertEmptyBodyAndHttpCodeIs(400)

        mockWebServer.takeRequest().verifyMultipartUploadRequestHeadersAndBody()
    }

    @Test
    fun serverInterruptedMultipartUpload() {
        mockWebServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_DURING_REQUEST_BODY))

        val uploadRequest = createMultipartUploadRequest()

        val exception = uploadRequest.getBlockingResponse(appContext).requireOtherError()

        assertTrue(
            "A subclass of IOException has to be thrown. Got ${exception::class.java}",
            exception is IOException
        )
    }

    @Test
    fun userCancelledMultipartUpload() {
        mockWebServer.enqueue(
            MockResponse()
                .throttleBody(100, 10, TimeUnit.MILLISECONDS)
                .setResponseCode(200)
        )

        val uploadRequest = createMultipartUploadRequest()

        uploadRequest.getBlockingResponse(appContext, doOnFirstProgress = { info ->
            // cancel upload on first progress
            UploadService.stopUpload(info.uploadId)
        }).requireCancelledByUser()

        with(mockWebServer.takeRequest()) {
            assertHttpMethodIs("POST")
            assertContentTypeIsMultipartFormData()
            assertBodySizeIsLowerOrEqualThanDeclaredContentLength()
            assertHeader("Authorization", "Bearer bearerToken")
            assertHeader("User-Agent", "SomeUserAgent")
        }
    }

    @Test
    fun multipleCallsToStartUpload() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val uploadRequest = createMultipartUploadRequest()

        uploadRequest.startUpload()

        try {
            uploadRequest.startUpload()
            fail("This should throw an exception")
        } catch (exc: Throwable) {
            assertTrue("$exc", exc is IllegalStateException)
            assertTrue("$exc", exc.message?.startsWith("You have already called startUpload() on this Upload request instance once") ?: false)
        }
    }

    @Test
    fun multipleUploadsWithSameID() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setHeadersDelay(2, TimeUnit.SECONDS))

        val uploadID = UUID.randomUUID().toString()

        createMultipartUploadRequest()
            .setUploadID(uploadID)
            .startUpload()

        Thread.sleep(1000)

        try {
            createMultipartUploadRequest()
                .setUploadID(uploadID)
                .startUpload()
            fail("This should throw an exception")
        } catch (exc: Throwable) {
            assertTrue("$exc", exc is IllegalStateException)
            assertTrue("$exc", exc.message?.startsWith("You have tried to perform startUpload() using the same uploadID") ?: false)
        }
    }
}
