package com.levin.uploadservice.data

import android.content.Intent
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.levin.uploadservice.UploadServiceConfig
import com.levin.uploadservice.extensions.parcelableCompat
import com.levin.uploadservice.network.ServerResponse

@Parcelize
internal data class BroadcastData @JvmOverloads constructor(
    val status: UploadStatus,
    val uploadInfo: UploadInfo,
    val serverResponse: ServerResponse? = null,
    val exception: Throwable? = null
) : Parcelable {
    companion object {
        private const val paramName = "broadcastData"

        fun fromIntent(intent: Intent): BroadcastData? {
            return intent.parcelableCompat(paramName)
        }
    }

    fun toIntent() = Intent(UploadServiceConfig.broadcastStatusAction).apply {
        setPackage(UploadServiceConfig.namespace)
        putExtra(paramName, this@BroadcastData)
    }
}
