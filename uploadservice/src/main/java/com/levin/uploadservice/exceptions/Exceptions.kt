package com.levin.uploadservice.exceptions

import com.levin.uploadservice.network.ServerResponse

class UserCancelledUploadException : Throwable("User cancelled upload")
class UploadError(val serverResponse: ServerResponse) : Throwable("Upload error")
