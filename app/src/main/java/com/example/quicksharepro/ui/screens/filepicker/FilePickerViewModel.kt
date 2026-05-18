package com.example.quicksharepro.ui.screens.filepicker

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.quicksharepro.domain.model.TransferFile
import com.example.quicksharepro.domain.repository.FileSelectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class FilePickerViewModel @Inject constructor(
    private val selectionRepository: FileSelectionRepository
) : ViewModel() {
    val selectedFiles = selectionRepository.selectedFiles

    fun addFiles(uris: List<Uri>, resolver: android.content.ContentResolver) {
        val newFiles = uris.map { uri ->
            var name = "Unknown"
            var size = 0L
            val cursor = resolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    if (nameIndex != -1) name = it.getString(nameIndex)
                    if (sizeIndex != -1) size = it.getLong(sizeIndex)
                }
            }
            TransferFile(name, size, resolver.getType(uri) ?: "application/octet-stream", uri)
        }
        selectionRepository.addFiles(newFiles)
    }

    fun removeFile(file: TransferFile) {
        selectionRepository.removeFile(file)
    }
}
