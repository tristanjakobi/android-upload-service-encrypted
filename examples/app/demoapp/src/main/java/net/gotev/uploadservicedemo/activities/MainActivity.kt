package com.levin.uploadservicedemo.activities

import android.os.Bundle
import android.widget.Button
import com.levin.uploadservice.UploadService
import com.levin.uploadservicedemo.R
import com.levin.uploadservicedemo.extensions.openBrowser
import com.levin.uploadservicedemo.views.AddItem

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<AddItem>(R.id.multipart_upload).setOnClickListener {
            MultipartUploadActivity.show(this)
        }

        findViewById<AddItem>(R.id.binary_upload).setOnClickListener {
            BinaryUploadActivity.show(this)
        }

        findViewById<AddItem>(R.id.ftp_upload).setOnClickListener {
            FTPUploadActivity.show(this)
        }

        findViewById<Button>(R.id.cancelAllUploadsButton).setOnClickListener {
            UploadService.stopAllUploads()
        }

        findViewById<Button>(R.id.visitHome).setOnClickListener {
            openBrowser("https://github.com/gotev/android-upload-service")
        }
    }
}
