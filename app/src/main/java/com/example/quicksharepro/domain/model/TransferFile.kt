package com.example.quicksharepro.domain.model

import android.net.Uri

data class TransferFile(
    val name: String,
    val size: Long,
    val mimeType: String,
    val uri: Uri? = null,
    val path: String? = null
)
