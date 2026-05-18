package com.example.quicksharepro.data.repository

import com.example.quicksharepro.domain.model.TransferFile
import com.example.quicksharepro.domain.repository.FileSelectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileSelectionRepositoryImpl @Inject constructor() : FileSelectionRepository {
    private val _selectedFiles = MutableStateFlow<List<TransferFile>>(emptyList())
    override val selectedFiles: StateFlow<List<TransferFile>> = _selectedFiles.asStateFlow()

    override fun addFiles(files: List<TransferFile>) {
        _selectedFiles.value = _selectedFiles.value + files
    }

    override fun removeFile(file: TransferFile) {
        _selectedFiles.value = _selectedFiles.value - file
    }

    override fun clearSelection() {
        _selectedFiles.value = emptyList()
    }
}

