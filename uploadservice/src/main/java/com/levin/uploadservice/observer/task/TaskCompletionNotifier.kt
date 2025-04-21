package com.levin.uploadservice.observer.task

import com.levin.uploadservice.UploadService
import com.levin.uploadservice.data.UploadInfo
import com.levin.uploadservice.data.UploadNotificationConfig
import com.levin.uploadservice.network.ServerResponse

class TaskCompletionNotifier(private val service: UploadService) : UploadTaskObserver {
    override fun onStart(
        info: UploadInfo,
        notificationId: Int,
        notificationConfig: UploadNotificationConfig
    ) {
    }

    override fun onProgress(
        info: UploadInfo,
        notificationId: Int,
        notificationConfig: UploadNotificationConfig
    ) {
    }

    override fun onSuccess(
        info: UploadInfo,
        notificationId: Int,
        notificationConfig: UploadNotificationConfig,
        response: ServerResponse
    ) {
    }

    override fun onError(
        info: UploadInfo,
        notificationId: Int,
        notificationConfig: UploadNotificationConfig,
        exception: Throwable
    ) {
    }

    override fun onCompleted(
        info: UploadInfo,
        notificationId: Int,
        notificationConfig: UploadNotificationConfig
    ) {
        service.taskCompleted(info.uploadId)
    }
}
