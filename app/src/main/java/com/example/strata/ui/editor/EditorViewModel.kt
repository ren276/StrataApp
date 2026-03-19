package com.example.strata.ui.editor

import android.graphics.Bitmap
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.strata.data.local.ActivityEntity
import com.example.strata.data.repository.ActivityRepository
import com.example.strata.data.model.Template
import com.example.strata.data.repository.MockTemplateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

class EditorViewModel(
    private val activityId: Long,
    private val templateId: String? = null,
    private val activityRepository: ActivityRepository,
    private val authRepository: com.example.strata.data.auth.AuthRepository
) : ViewModel() {

    private val _activity = MutableStateFlow<ActivityEntity?>(null)
    val activity: StateFlow<ActivityEntity?> = _activity.asStateFlow()

    private val _templateTitle = MutableStateFlow("")
    val templateTitle: StateFlow<String> = _templateTitle.asStateFlow()

    private val _templateDate = MutableStateFlow("")
    val templateDate: StateFlow<String> = _templateDate.asStateFlow()

    private val _templateDistance = MutableStateFlow("")
    val templateDistance: StateFlow<String> = _templateDistance.asStateFlow()

    private val _templateDuration = MutableStateFlow("")
    val templateDuration: StateFlow<String> = _templateDuration.asStateFlow()

    private val _templateElevation = MutableStateFlow("")
    val templateElevation: StateFlow<String> = _templateElevation.asStateFlow()

    private val _templatePrimaryColor = MutableStateFlow<androidx.compose.ui.graphics.Color?>(null)
    val templatePrimaryColor: StateFlow<androidx.compose.ui.graphics.Color?> = _templatePrimaryColor.asStateFlow()

    private val _templateSecondaryColor = MutableStateFlow<androidx.compose.ui.graphics.Color?>(null)
    val templateSecondaryColor: StateFlow<androidx.compose.ui.graphics.Color?> = _templateSecondaryColor.asStateFlow()

    // Unified Editor Elements
    sealed class EditorElement {
        abstract val id: String
        abstract val x: Float
        abstract val y: Float
        abstract val rotationX: Float
        abstract val rotationY: Float
        abstract val rotationZ: Float
        abstract val scale: Float
        
        data class Text(
            override val id: String = java.util.UUID.randomUUID().toString(),
            val text: String = "New Text",
            val color: Long = 0xFFFFFFFF,
            val font: FontType = FontType.DEFAULT,
            val fontSize: Int = 24,
            val textAlign: TextAlign = TextAlign.CENTER,
            override val x: Float = 0.5f,
            override val y: Float = 0.5f,
            override val rotationX: Float = 0f,
            override val rotationY: Float = 0f,
            override val rotationZ: Float = 0f,
            override val scale: Float = 1f
        ) : EditorElement()
        
        data class Data(
            override val id: String = java.util.UUID.randomUUID().toString(),
            val type: DataType,
            val value: String,
            val label: String,
            val color: Long = 0xFFFFFFFF,
            val font: FontType = FontType.DEFAULT,
            val fontSize: Int = 32,
            val textAlign: TextAlign = TextAlign.CENTER,
            override val x: Float = 0.5f,
            override val y: Float = 0.5f,
            override val rotationX: Float = 0f,
            override val rotationY: Float = 0f,
            override val rotationZ: Float = 0f,
            override val scale: Float = 1f
        ) : EditorElement()
    }
    
    enum class FontType {
        DEFAULT, SERIF, MONOSPACE, CURSIVE, BOLD, BEBAS_NEUE, PLAYFAIR
    }
    
    enum class DataType {
        TITLE, DATE, DISTANCE, TIME, ELEVATION, CALORIES, HEARTRATE
    }

    enum class TextAlign {
        LEFT, CENTER, RIGHT
    }

    private val _elements = MutableStateFlow<List<EditorElement>>(emptyList())
    val elements: StateFlow<List<EditorElement>> = _elements.asStateFlow()
    
    private val _selectedElementId = MutableStateFlow<String?>(null)
    val selectedElementId: StateFlow<String?> = _selectedElementId.asStateFlow()
    
    // Background State (Unchanged)
    private val _background = MutableStateFlow<BackgroundType>(BackgroundType.Image("https://lh3.googleusercontent.com/aida-public/AB6AXuDrI0gz1sC1PHS-4MKnpWr6POsS8lpvvDYTU7ChYaTQDaxfqwRXBT10t0OdhLMnN0kcL55f8n2gDU8afFrsQ5lmry2T1lJqiuDVTQo17U4jLdPihqaqMjy6f5rM2xpsRQci5QEI3YOmWkG88hPu8eMFFy8Vpy_RqSEiH4qSKq4Bsg7r3-76rizt4JC1HqI7YEzSeKvkPvQuzerHQrcFBe6aSFUt4YowJjCDLBH78Wy2fOlwOGHdvNctl3jSe38dKPJsjRDMQD_O3ag"))
    val background: StateFlow<BackgroundType> = _background.asStateFlow()

    init {
        loadActivity()
    }

    private fun loadActivity() {
        viewModelScope.launch {
            activityRepository.getActivity(activityId).collect { entity ->
                _activity.value = entity
                if (entity != null) {
                    _templateTitle.value = entity.title
                    _templateDate.value = formatDateFriendly(entity.date)
                    _templateDistance.value = String.format(java.util.Locale.US, "%.2f KM", entity.distance / 1000f)
                    val durationMin = entity.movingTime / 60
                    _templateDuration.value = "${durationMin / 60}H ${durationMin % 60}M"
                    _templateElevation.value = "${entity.totalElevationGain.toInt()}M ELEV"

                    if (_elements.value.isEmpty()) {
                        if (templateId != null) {
                            applyTemplate(templateId)
                        } else {
                            initializeDefaultElements(entity)
                        }
                    }
                    
                    // Specific Logic: If we have photos but no URL, fetch details
                    if (entity.totalPhotos > 0 && entity.photoUrl.isNullOrEmpty()) {
                        launch(Dispatchers.IO) {
                           activityRepository.fetchActivityDetails(activityId)
                        }
                    }
                    
                    // Specific Logic: If photo URL arrives and we are on default background (or it was missing), update it
                    if (!entity.photoUrl.isNullOrEmpty()) {
                         val currentBg = _background.value
                         // If background is Map or Gradient (fallbacks), upgrade to Photo
                         // Or if we just want to ensure the photo is shown.
                         // Let's be careful not to override user choice if they manually picked something else.
                         // But for initial load or "repair", we should likely show it.
                         // Simple heuristic: If background is Image but URL doesn't match, or if it's NOT Image/Video
                         if (currentBg !is BackgroundType.Image && currentBg !is BackgroundType.Video) {
                              setBackgroundImage(entity.photoUrl)
                         }
                    }
                }
            }
        }
    }
    
    private fun initializeDefaultElements(activity: ActivityEntity) {
        val defaultElements = mutableListOf<EditorElement>()
        
        // Title
        defaultElements.add(EditorElement.Data(
            type = DataType.TITLE,
            value = activity.title,
            label = "",
            x = 0.5f, y = 0.2f,
            fontSize = 32
        ))
        
        // Date
        defaultElements.add(EditorElement.Data(
            type = DataType.DATE,
            value = formatDateFriendly(activity.date),
            label = "",
            x = 0.5f, y = 0.25f,
            fontSize = 14
        ))
        
        // Stats Row 1
        defaultElements.add(EditorElement.Data(
            type = DataType.DISTANCE,
            value = String.format("%.2f", activity.distance / 1000),
            label = "KM",
            x = 0.2f, y = 0.8f
        ))
        
         defaultElements.add(EditorElement.Data(
            type = DataType.TIME,
            value = formatDuration(activity.movingTime),
            label = "TIME",
            x = 0.5f, y = 0.8f
        ))
        
         defaultElements.add(EditorElement.Data(
            type = DataType.ELEVATION,
            value = "${activity.totalElevationGain.toInt()}",
            label = "M",
            x = 0.8f, y = 0.8f
        ))
        
        _elements.value = defaultElements
    }
    
    // Actions
    
    fun updateElementText(id: String, text: String) {
        updateElement(id) { 
             when (it) {
                is EditorElement.Text -> it.copy(text = text)
                is EditorElement.Data -> it // Data elements don't have editable text value in the same way (computed from value)
            }
        }
    }

    fun addData(type: DataType) {
        val currentActivity = _activity.value ?: return
        val newElement = when (type) {
            DataType.TITLE -> EditorElement.Data(type = type, value = currentActivity.title, label = "")
            DataType.DATE -> {
                 EditorElement.Data(type = type, value = formatDateFriendly(currentActivity.date), label = "", fontSize = 14)
            }
            DataType.DISTANCE -> EditorElement.Data(type = type, value = String.format("%.2f", currentActivity.distance / 1000), label = "KM")
            DataType.TIME -> EditorElement.Data(type = type, value = formatDuration(currentActivity.movingTime), label = "TIME")
            DataType.ELEVATION -> EditorElement.Data(type = type, value = "${currentActivity.totalElevationGain.toInt()}", label = "M")
            DataType.CALORIES -> EditorElement.Data(type = type, value = "0", label = "KCAL") // Placeholder
            DataType.HEARTRATE -> EditorElement.Data(type = type, value = "0", label = "BPM") // Placeholder as entity doesn't have cals yet
        }
        _elements.value = _elements.value + newElement
        selectElement(newElement.id)
    }
    
    fun selectElement(id: String?) {
        _selectedElementId.value = id
    }
    
    fun updateElementPosition(id: String, x: Float, y: Float) {
        updateElement(id) { 
            when (it) {
                is EditorElement.Text -> it.copy(x = x, y = y)
                is EditorElement.Data -> it.copy(x = x, y = y)
            }
        }
    }
    
    fun updateElementRotation(id: String, rx: Float, ry: Float, rz: Float) {
        updateElement(id) { 
            when (it) {
                is EditorElement.Text -> it.copy(rotationX = rx, rotationY = ry, rotationZ = rz)
                is EditorElement.Data -> it.copy(rotationX = rx, rotationY = ry, rotationZ = rz)
            }
        }
    }
    
    fun updateElementScale(id: String, scale: Float) {
        updateElement(id) { 
             when (it) {
                is EditorElement.Text -> it.copy(scale = scale)
                is EditorElement.Data -> it.copy(scale = scale)
            }
        }
    }

    fun updateTextColor(id: String, color: Long) {
        updateElement(id) {
            when (it) {
                is EditorElement.Text -> it.copy(color = color)
                is EditorElement.Data -> it.copy(color = color)
            }
        }
    }

    fun updateTextFont(id: String, font: FontType) {
        updateElement(id) {
            when (it) {
                is EditorElement.Text -> it.copy(font = font)
                is EditorElement.Data -> it.copy(font = font)
            }
        }
    }

    fun updateElementAlignment(id: String, textAlign: TextAlign) {
        updateElement(id) {
            when (it) {
                is EditorElement.Text -> it.copy(textAlign = textAlign)
                is EditorElement.Data -> it.copy(textAlign = textAlign)
            }
        }
    }

    fun setTemplateTitle(title: String) { _templateTitle.value = title }
    fun setTemplateDate(date: String) { _templateDate.value = date }
    fun setTemplateDistance(distance: String) { _templateDistance.value = distance }
    fun setTemplateDuration(duration: String) { _templateDuration.value = duration }
    fun setTemplateElevation(elevation: String) { _templateElevation.value = elevation }
    fun setTemplatePrimaryColor(color: androidx.compose.ui.graphics.Color) { _templatePrimaryColor.value = color }
    fun setTemplateSecondaryColor(color: androidx.compose.ui.graphics.Color) { _templateSecondaryColor.value = color }
    
    fun addText() {
        val newText = EditorElement.Text()
        _elements.value = _elements.value + newText
        selectElement(newText.id)
    }
    
    fun removeElement(id: String) {
        _elements.value = _elements.value.filter { it.id != id }
        if (_selectedElementId.value == id) {
            _selectedElementId.value = null
        }
    }

    private fun updateElement(id: String, transform: (EditorElement) -> EditorElement) {
        _elements.value = _elements.value.map { if (it.id == id) transform(it) else it }
    }

    fun applyTemplate(templateId: String) {
        val template = com.example.strata.data.model.ActivityTemplate.allTemplates.find { it.id == templateId }
        val mockTemplate = com.example.strata.data.repository.MockTemplateRepository.getTemplate(templateId)
        val activity = _activity.value ?: return

        if (mockTemplate != null) {
            // 1. Set Background
            if (mockTemplate.backgroundUrl.isNotEmpty()) {
                setBackgroundImage(mockTemplate.backgroundUrl)
            }
            // 2. Map Elements
            val newElements = mockTemplate.elements.map { templateElement ->
                when (templateElement) {
                    is EditorElement.Data -> {
                        val value = when (templateElement.type) {
                            DataType.DISTANCE -> String.format(java.util.Locale.US, "%.2f", activity.distance / 1000f)
                            DataType.TIME -> formatDuration(activity.movingTime)
                            DataType.ELEVATION -> activity.totalElevationGain.toInt().toString()
                            DataType.CALORIES -> "0"
                            DataType.HEARTRATE -> "0"
                            DataType.TITLE -> activity.title
                            DataType.DATE -> formatDateFriendly(activity.date)
                        }
                        templateElement.copy(value = value, id = java.util.UUID.randomUUID().toString())
                    }
                    is EditorElement.Text -> templateElement.copy(id = java.util.UUID.randomUUID().toString())
                }
            }
            _elements.value = newElements
        } else if (template != null) {
            // ActivityTemplate: Canvas renders ALL data visually.
            // No freeform elements spawned — avoids duplication with the Canvas layer.
            _elements.value = emptyList()
        }
        selectElement(null)
    }

    // Background Setters
    fun setBackgroundImage(uri: String) { _background.value = BackgroundType.Image(uri) }
    fun setBackgroundVideo(uri: String) { _background.value = BackgroundType.Video(uri) }
    fun setBackgroundGradient(colors: List<Long>) { _background.value = BackgroundType.Gradient(colors) }
    fun setBackgroundMap(polyline: String) { _background.value = BackgroundType.Map(polyline) }

    /**
     * Receives the baked [Bitmap] produced by [ImageEditorScreen] and sets it
     * as the current background so the editor canvas shows the edited image.
     */
    fun setEditedBackground(bitmap: Bitmap) {
        _background.value = BackgroundType.EditedBitmap(bitmap)
    }
    
    private fun formatDuration(seconds: Int): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) String.format("%d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s)
    }

    private fun formatDateFriendly(instant: java.time.Instant): String {
        val zoneId = java.time.ZoneId.systemDefault()
        val zdt = instant.atZone(zoneId)
        val day = zdt.dayOfMonth
        val suffix = getDayOfMonthSuffix(day)
        val formatter = java.time.format.DateTimeFormatter.ofPattern("d'$suffix' MMM yyyy 'at' h:mma", java.util.Locale.getDefault())
        return formatter.format(zdt).replace("AM", "am").replace("PM", "pm") 
    }

    private fun getDayOfMonthSuffix(n: Int): String {
        if (n in 11..13) return "th"
        return when (n % 10) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    }

    sealed class BackgroundType {
        data class Image(val uri: String) : BackgroundType()
        data class Video(val uri: String) : BackgroundType()
        data class Gradient(val colors: List<Long>) : BackgroundType()
        data class Map(val polyline: String) : BackgroundType()
        /** A bitmap produced by [ImageEditorScreen] with rotation/zoom/pan already baked in. */
        data class EditedBitmap(val bitmap: Bitmap) : BackgroundType()
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val activityId: Long,
        private val templateId: String? = null,
        private val repository: ActivityRepository,
        private val authRepository: com.example.strata.data.auth.AuthRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EditorViewModel::class.java)) {
                return EditorViewModel(activityId, templateId, repository, authRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
