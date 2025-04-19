package com.levin.uploadservice.observer.request

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.levin.uploadservice.UploadService
import com.levin.uploadservice.UploadServiceConfig.broadcastNotificationAction
import com.levin.uploadservice.UploadServiceConfig.broadcastNotificationActionIntentFilter
import com.levin.uploadservice.extensions.registerReceiverCompat
import com.levin.uploadservice.extensions.uploadIdToCancel
import com.levin.uploadservice.logger.UploadServiceLogger
import com.levin.uploadservice.logger.UploadServiceLogger.NA

open class NotificationActionsObserver(
    private val context: Context
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != broadcastNotificationAction) return
        onActionIntent(intent)
    }

    open fun onActionIntent(intent: Intent) {
        intent.uploadIdToCancel?.let {
            UploadServiceLogger.info(NotificationActionsObserver::class.java.simpleName, it) {
                "requested upload cancellation"
            }
            UploadService.stopUpload(it)
        }
    }

    fun register() {
        context.registerReceiverCompat(this, broadcastNotificationActionIntentFilter)
        UploadServiceLogger.debug(NotificationActionsObserver::class.java.simpleName, NA) {
            "registered"
        }
    }

    fun unregister() {
        context.unregisterReceiver(this)
        UploadServiceLogger.debug(NotificationActionsObserver::class.java.simpleName, NA) {
            "unregistered"
        }
    }
}
