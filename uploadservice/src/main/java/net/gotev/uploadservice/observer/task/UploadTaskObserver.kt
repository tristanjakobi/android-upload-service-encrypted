package com.levin.uploadservice.observer.task

import com.levin.uploadservice.data.UploadInfo
import com.levin.uploadservice.data.UploadNotificationConfig
import com.levin.uploadservice.network.ServerResponse

interface UploadTaskObserver {
    fun onStart(info: UploadInfo, notificationId: Int, notificationConfig: UploadNotificationConfig)

    fun onProgress(
        info: UploadInfo,
        notificationId: Int,
        notificationConfig: UploadNotificationConfig
    )

    fun onSuccess(
        info: UploadInfo,
        notificationId: Int,
        notificationConfig: UploadNotificationConfig,
        response: ServerResponse
    )

    fun onError(
        info: UploadInfo,
        notificationId: Int,
        notificationConfig: UploadNotificationConfig,
        exception: Throwable
    )

    fun onCompleted(
        info: UploadInfo,
        notificationId: Int,
        notificationConfig: UploadNotificationConfig
    )
}
