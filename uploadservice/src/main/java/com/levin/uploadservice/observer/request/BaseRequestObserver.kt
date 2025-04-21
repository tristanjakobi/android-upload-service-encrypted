package com.levin.uploadservice.observer.request

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.levin.uploadservice.UploadServiceConfig
import com.levin.uploadservice.data.BroadcastData
import com.levin.uploadservice.data.UploadInfo
import com.levin.uploadservice.data.UploadStatus
import com.levin.uploadservice.extensions.registerReceiverCompat

open class BaseRequestObserver(
    private val context: Context,
    internal val delegate: RequestObserverDelegate,
    internal var shouldAcceptEventsFrom: (uploadInfo: UploadInfo) -> Boolean
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val safeIntent = intent ?: return
        if (safeIntent.action != UploadServiceConfig.broadcastStatusAction) return
        val data = BroadcastData.fromIntent(safeIntent) ?: return

        val uploadInfo = data.uploadInfo

        if (!shouldAcceptEventsFrom(uploadInfo)) {
            return
        }

        when (data.status) {
            UploadStatus.InProgress -> delegate.onProgress(context, uploadInfo)
            UploadStatus.Error -> delegate.onError(context, uploadInfo, data.exception!!)
            UploadStatus.Success -> delegate.onSuccess(context, uploadInfo, data.serverResponse!!)
            UploadStatus.Completed -> delegate.onCompleted(context, uploadInfo)
        }
    }

    open fun register() {
        context.registerReceiverCompat(this, UploadServiceConfig.broadcastStatusIntentFilter)
    }

    open fun unregister() {
        context.unregisterReceiver(this)
    }
}
