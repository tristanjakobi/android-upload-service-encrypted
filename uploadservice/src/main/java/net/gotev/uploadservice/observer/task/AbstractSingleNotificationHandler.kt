package com.levin.uploadservice.observer.task

import android.app.NotificationManager
import android.content.Context
import android.os.Parcelable
import androidx.core.app.NotificationCompat
import kotlinx.parcelize.Parcelize
import com.levin.uploadservice.UploadService
import com.levin.uploadservice.UploadServiceConfig
import com.levin.uploadservice.data.UploadInfo
import com.levin.uploadservice.data.UploadNotificationConfig
import com.levin.uploadservice.data.UploadNotificationStatusConfig
import com.levin.uploadservice.exceptions.UserCancelledUploadException
import com.levin.uploadservice.extensions.validateNotificationChannel
import com.levin.uploadservice.network.ServerResponse
import java.util.concurrent.ConcurrentHashMap

abstract class AbstractSingleNotificationHandler(
    private val service: UploadService
) : UploadTaskObserver {

    enum class TaskStatus {
        InProgress,
        Succeeded,
        Failed,
        Cancelled
    }

    @Parcelize
    data class TaskData(
        val status: TaskStatus,
        val info: UploadInfo,
        val config: UploadNotificationStatusConfig
    ) : Parcelable

    private val tasks = ConcurrentHashMap<String, TaskData>()

    private val notificationManager by lazy {
        service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    init {
        notificationManager.validateNotificationChannel(UploadServiceConfig.defaultNotificationChannel!!)
    }

    fun removeTask(uploadId: String) {
        tasks.remove(uploadId)
    }

    abstract fun updateNotification(
        notificationManager: NotificationManager,
        notificationBuilder: NotificationCompat.Builder,
        tasks: Map<String, TaskData>
    ): NotificationCompat.Builder?

    @Synchronized
    private fun updateTask(
        status: TaskStatus,
        info: UploadInfo,
        config: UploadNotificationStatusConfig
    ) {
        tasks[info.uploadId] = TaskData(status, info, config)

        val builder =
            NotificationCompat.Builder(service, UploadServiceConfig.defaultNotificationChannel!!)
        val notification = updateNotification(notificationManager, builder, HashMap(tasks))
            ?.setGroup(UploadServiceConfig.namespace)
            ?.setOngoing(true)
            ?.build()
            ?: return

        service.holdForegroundNotification(javaClass.name, notification)
    }

    override fun onStart(
        info: UploadInfo,
        notificationId: Int,
        notificationConfig: UploadNotificationConfig
    ) {
        updateTask(TaskStatus.InProgress, info, notificationConfig.progress)
    }

    override fun onProgress(
        info: UploadInfo,
        notificationId: Int,
        notificationConfig: UploadNotificationConfig
    ) {
        updateTask(TaskStatus.InProgress, info, notificationConfig.progress)
    }

    override fun onSuccess(
        info: UploadInfo,
        notificationId: Int,
        notificationConfig: UploadNotificationConfig,
        response: ServerResponse
    ) {
        updateTask(TaskStatus.Succeeded, info, notificationConfig.success)
    }

    override fun onError(
        info: UploadInfo,
        notificationId: Int,
        notificationConfig: UploadNotificationConfig,
        exception: Throwable
    ) {
        if (exception is UserCancelledUploadException) {
            updateTask(TaskStatus.Cancelled, info, notificationConfig.cancelled)
        } else {
            updateTask(TaskStatus.Failed, info, notificationConfig.cancelled)
        }
    }

    override fun onCompleted(
        info: UploadInfo,
        notificationId: Int,
        notificationConfig: UploadNotificationConfig
    ) {
    }
}
