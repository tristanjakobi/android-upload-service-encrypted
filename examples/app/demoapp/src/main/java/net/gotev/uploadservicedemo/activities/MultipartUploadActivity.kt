package com.levin.uploadservicedemo.activities

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.levin.recycleradapter.AdapterItem
import com.levin.uploadservice.data.UploadInfo
import com.levin.uploadservice.network.ServerResponse
import com.levin.uploadservice.observer.request.RequestObserverDelegate
import com.levin.uploadservice.protocols.multipart.MultipartUploadRequest
import com.levin.uploadservicedemo.R
import com.levin.uploadservicedemo.adapteritems.EmptyItem
import com.levin.uploadservicedemo.adapteritems.UploadItem
import com.levin.uploadservicedemo.extensions.openBrowser
import com.levin.uploadservicedemo.utils.UploadItemUtils
import com.levin.uploadservicedemo.utils.UploadItemUtils.ForEachDelegate
import java.io.IOException

class MultipartUploadActivity : HttpUploadActivity() {

    companion object {
        fun show(activity: BaseActivity) {
            activity.startActivity(Intent(activity, MultipartUploadActivity::class.java))
        }
    }

    override fun onInfo() {
        openBrowser("https://github.com/gotev/android-upload-service/wiki/4.x-Usage#http-multipartform-data-upload--rfc2388")
    }

    override val emptyItem: AdapterItem<*>
        get() = EmptyItem(getString(R.string.empty_multipart_upload))

    override fun onDone(httpMethod: String, serverUrl: String, uploadItemUtils: UploadItemUtils) {
        try {
            val request = MultipartUploadRequest(this, serverUrl)
                .setMethod(httpMethod)
                .setNotificationConfig { _: Context, uploadId: String ->
                    getNotificationConfig(uploadId, R.string.multipart_upload)
                }

            uploadItemUtils.forEach(object : ForEachDelegate {
                override fun onHeader(item: UploadItem) {
                    try {
                        request.addHeader(item.title, item.subtitle)
                    } catch (exc: IllegalArgumentException) {
                        Toast.makeText(
                            this@MultipartUploadActivity,
                            exc.message, Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onParameter(item: UploadItem) {
                    request.addParameter(item.title, item.subtitle)
                }

                override fun onFile(item: UploadItem) {
                    try {
                        request.addFileToUpload(item.subtitle, item.title)
                    } catch (exc: IOException) {
                        Toast.makeText(
                            this@MultipartUploadActivity,
                            getString(R.string.file_not_found, item.subtitle),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            })

            request.subscribe(this, this, object : RequestObserverDelegate {
                override fun onProgress(context: Context, uploadInfo: UploadInfo) {
                    Log.e("LIFECYCLE", "Progress " + uploadInfo.progressPercent)
                }

                override fun onSuccess(
                    context: Context,
                    uploadInfo: UploadInfo,
                    serverResponse: ServerResponse
                ) {
                    Log.e("LIFECYCLE", "Success " + uploadInfo.progressPercent)
                }

                override fun onError(
                    context: Context,
                    uploadInfo: UploadInfo,
                    exception: Throwable
                ) {
                    Log.e("LIFECYCLE", "Error " + exception.message)
                }

                override fun onCompleted(context: Context, uploadInfo: UploadInfo) {
                    Log.e("LIFECYCLE", "Completed ")
                    finish()
                }

                override fun onCompletedWhileNotObserving() {
                    Log.e("LIFECYCLE", "Completed while not observing")
                    finish()
                }
            })
        } catch (exc: Exception) {
            Toast.makeText(this, exc.message, Toast.LENGTH_LONG).show()
        }
    }
}
