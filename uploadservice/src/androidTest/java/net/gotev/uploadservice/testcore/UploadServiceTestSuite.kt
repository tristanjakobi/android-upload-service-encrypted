package com.levin.uploadservice.testcore

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import com.levin.uploadservice.UploadService
import com.levin.uploadservice.UploadServiceConfig
import org.junit.After
import org.junit.Before

open class UploadServiceTestSuite {

    val appContext: Application
        get() = InstrumentationRegistry.getInstrumentation().context.applicationContext as Application

    val mockWebServer = newSSLMockWebServer()

    @Before
    fun setup() {
        mockWebServer.start(8080)
        UploadServiceConfig.initialize(appContext, appContext.createTestNotificationChannel(), true)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
        appContext.deleteTestNotificationChannel()
        UploadService.stop(appContext, true)
    }
}
