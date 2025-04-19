package com.levin.uploadservicedemo.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.levin.recycleradapter.AdapterItem
import com.levin.uploadservice.protocols.binary.BinaryUploadRequest
import com.levin.uploadservicedemo.R
import com.levin.uploadservicedemo.adapteritems.EmptyItem
import com.levin.uploadservicedemo.adapteritems.UploadItem
import com.levin.uploadservicedemo.extensions.openBrowser
import com.levin.uploadservicedemo.utils.UploadItemUtils
import com.levin.uploadservicedemo.utils.UploadItemUtils.ForEachDelegate
import java.io.IOException

class BinaryUploadActivity : HttpUploadActivity() {
    companion object {
        fun show(activity: BaseActivity) {
            activity.startActivity(Intent(activity, BinaryUploadActivity::class.java))
        }
    }

    override fun onInfo() {
        openBrowser("https://github.com/gotev/android-upload-service/wiki/4.x-Usage#http-binary-upload-")
    }

    override val emptyItem: AdapterItem<*>
        get() = EmptyItem(getString(R.string.empty_binary_upload))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addParameter.visibility = View.GONE

        addFile.setTitleText(getString(R.string.set_file))
        addFile.setOnClickListener {
            fileParameterName = "file"
            openFilePicker()
        }
    }

    override fun onDone(httpMethod: String, serverUrl: String, uploadItemUtils: UploadItemUtils) {
        try {
            val request = BinaryUploadRequest(this, serverUrl)
                .setMethod(httpMethod)
                .setNotificationConfig { _: Context, uploadId: String ->
                    getNotificationConfig(uploadId, R.string.binary_upload)
                }

            uploadItemUtils.forEach(object : ForEachDelegate {
                override fun onHeader(item: UploadItem) {
                    request.addHeader(item.title, item.subtitle)
                }

                override fun onParameter(item: UploadItem) {
                    // Binary uploads does not support parameters
                }

                override fun onFile(item: UploadItem) {
                    try {
                        request.setFileToUpload(item.subtitle)
                    } catch (exc: IOException) {
                        Toast.makeText(
                            this@BinaryUploadActivity,
                            getString(R.string.file_not_found, item.subtitle),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            })
            request.startUpload()
            finish()
        } catch (exc: Exception) {
            Toast.makeText(this, exc.message, Toast.LENGTH_LONG).show()
        }
    }
}
