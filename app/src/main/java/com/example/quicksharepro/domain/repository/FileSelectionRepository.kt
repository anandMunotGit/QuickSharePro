package com.example.quicksharepro.domain.repository

import com.example.quicksharepro.domain.model.TransferFile
import kotlinx.coroutines.flow.StateFlow

interface FileSelectionRepository {
    val selectedFiles: StateFlow<List<TransferFile>>
    fun addFiles(files: List<TransferFile>)
    fun removeFile(file: TransferFile)
    fun clearSelection()
}
