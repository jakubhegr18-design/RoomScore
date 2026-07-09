package com.roomanalyzer.roomscore.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.roomanalyzer.roomscore.analysis.RoomAnalysis
import com.roomanalyzer.roomscore.analysis.RoomAnalyzer
import com.roomanalyzer.roomscore.data.HomeAssistantConfig
import com.roomanalyzer.roomscore.data.HomeAssistantRepository
import com.roomanalyzer.roomscore.data.HomeAssistantSensorData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RoomViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val MAX_PHOTOS = 4
    }

    private val haRepo = HomeAssistantRepository(application)

    var analysis by mutableStateOf<RoomAnalysis?>(null)
        private set

    var isAnalyzing by mutableStateOf(false)
        private set

    var photoCount by mutableIntStateOf(0)
        private set

    var capturedPhotos by mutableStateOf<List<Bitmap>>(emptyList())
        private set

    var haConfig by mutableStateOf(haRepo.loadConfig())
        private set

    var haSensorData by mutableStateOf<HomeAssistantSensorData?>(null)
        private set

    val isFinishedCapturing: Boolean get() = photoCount >= MAX_PHOTOS

    fun addPhoto(bitmap: Bitmap) {
        if (isAnalyzing) return
        capturedPhotos = capturedPhotos + bitmap
        photoCount = capturedPhotos.size
        if (photoCount >= MAX_PHOTOS) {
            analyzeAll()
        }
    }

    fun finishCapture() {
        if (capturedPhotos.isEmpty() || isAnalyzing) return
        analyzeAll()
    }

    fun updateHaConfig(config: HomeAssistantConfig) {
        haRepo.saveConfig(config)
        haConfig = config
    }

    fun clearHaConfig() {
        haRepo.clearConfig()
        haConfig = HomeAssistantConfig()
        haSensorData = null
    }

    private fun analyzeAll() {
        if (isAnalyzing) return
        isAnalyzing = true
        haSensorData = null
        val photos = capturedPhotos
        capturedPhotos = emptyList()
        viewModelScope.launch {
            try {
                val allLabels = withContext(Dispatchers.IO) {
                    val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
                    val all = mutableListOf<ImageLabel>()
                    for (bitmap in photos) {
                        try {
                            val image = InputImage.fromBitmap(bitmap, 0)
                            all.addAll(Tasks.await(labeler.process(image)))
                        } catch (_: Exception) { }
                    }
                    all
                }

                val envData = if (haConfig.isConfigured) {
                    haRepo.fetchSensorData(haConfig).also { haSensorData = it }
                } else null

                analysis = withContext(Dispatchers.Default) {
                    RoomAnalyzer.analyze(allLabels, envData)
                }
            } catch (_: Exception) { }
            isAnalyzing = false
        }
    }

    fun reset() {
        analysis = null
        isAnalyzing = false
        photoCount = 0
        capturedPhotos = emptyList()
    }
}
