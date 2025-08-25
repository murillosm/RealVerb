package com.example.projetotcc.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projetotcc.ui.DetectionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

enum class ChallengeStatus {
    AWAITING_USER_ACTION,
    CORRECT_OBJECT_DETECTED,
    WRONG_OBJECT_DETECTED,
    NO_OBJECT_DETECTED,
    MAX_ATTEMPTS_REACHED
}

class ObjectDetectionViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    var currentPhotoPath: String = ""

    private val _bitmap = MutableStateFlow<Bitmap?>(null)
    val bitmap: StateFlow<Bitmap?> = _bitmap.asStateFlow()

    private val _detectionResults = MutableStateFlow<List<DetectionResult>>(emptyList())
    val detectionResults: StateFlow<List<DetectionResult>> = _detectionResults.asStateFlow()

    private val _detectedLabel = MutableStateFlow("")
    val detectedLabel: StateFlow<String> = _detectedLabel.asStateFlow()

    private val _objectToFind = MutableStateFlow("")
    val objectToFind: StateFlow<String> = _objectToFind.asStateFlow()

    private val _challengeStatus = MutableStateFlow(ChallengeStatus.AWAITING_USER_ACTION)
    val challengeStatus: StateFlow<ChallengeStatus> = _challengeStatus.asStateFlow()

    private val _wrongAttempts = MutableStateFlow(0)
    val wrongAttempts: StateFlow<Int> = _wrongAttempts.asStateFlow()

    private val MAX_WRONG_ATTEMPTS = 3

    private var currentCategory: String = ""

    private val cocoCategoryMap = mapOf(
        "person" to listOf("person"),
        "vehicle" to listOf("bicycle", "car", "motorcycle", "airplane", "bus", "train", "truck", "boat"),
        "outdoor" to listOf("traffic light", "fire hydrant", "stop sign", "parking meter", "bench", "bird"),
        "animal" to listOf("cat", "dog", "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe"),
        "accessory" to listOf("backpack", "umbrella", "handbag", "tie", "suitcase"),
        "sports" to listOf("frisbee", "skis", "snowboard", "sports ball", "skateboard", "surfboard", "tennis racket", "kite"),
        "kitchen" to listOf("bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl"),
        "food" to listOf("banana", "apple", "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake"),
        "furniture" to listOf("chair", "couch", "potted plant", "bed", "dining table", "toilet"),
        "electronic" to listOf("tv", "laptop", "mouse", "remote", "keyboard", "cell phone"),
        "appliance" to listOf("microwave", "oven", "toaster", "sink", "refrigerator")
    )

    init {
        startNewChallenge()
    }

    fun setCategory(category: String) {
        if (category != currentCategory) {
            currentCategory = category
            startNewChallenge()
        }
    }

    fun startNewChallenge() {
        val objects = cocoCategoryMap[currentCategory] ?: listOf("person")
        _objectToFind.value = objects.random()
        _wrongAttempts.value = 0
        clearDetection()
    }

    fun tryAgain() {
        clearDetection()
    }

    fun onPictureTaken(context: Context, capturedBitmap: Bitmap) {
        _bitmap.value = capturedBitmap
        runObjectDetection(context, capturedBitmap)
    }

    private fun clearDetection() {
        _bitmap.value = null
        _detectionResults.value = emptyList()
        _detectedLabel.value = ""
        _challengeStatus.value = ChallengeStatus.AWAITING_USER_ACTION
        currentPhotoPath = ""
    }

    private fun updateUserPoints(pointsToAdd: Long) {
        if (pointsToAdd <= 0) return

        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .update("points", FieldValue.increment(pointsToAdd))
        }
    }

    private fun runObjectDetection(context: Context, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.Default) {
            val image = TensorImage.fromBitmap(bitmap)
            val options = ObjectDetector.ObjectDetectorOptions.builder()
                .setMaxResults(1)
                .setScoreThreshold(0.6f)
                .build()
            val detector = ObjectDetector.createFromFileAndOptions(context, "model.tflite", options)
            val results: List<Detection> = detector.detect(image)

            val detectedObjectLabel = results.firstOrNull()?.categories?.firstOrNull()?.label

            if (detectedObjectLabel == null) {
                _detectedLabel.value = "No object identified"
                _detectionResults.value = emptyList()
                _wrongAttempts.value++
                _challengeStatus.value = ChallengeStatus.NO_OBJECT_DETECTED
            } else {
                val resultToDisplay = results.map {
                    val category = it.categories.first()
                    val text = "${category.label}, ${category.score.times(100).toInt()}%"
                    DetectionResult(it.boundingBox, text)
                }
                _detectionResults.value = resultToDisplay
                _detectedLabel.value = resultToDisplay.first().text

                if (detectedObjectLabel.equals(_objectToFind.value, ignoreCase = true)) {
                    // 1 erro = 2 pontos, 2 erros = 1 ponto, 0 erros = 3 pontos
                    val points = (MAX_WRONG_ATTEMPTS - _wrongAttempts.value).toLong()
                    updateUserPoints(points)

                    _challengeStatus.value = ChallengeStatus.CORRECT_OBJECT_DETECTED
                } else {
                    _wrongAttempts.value++
                    _challengeStatus.value = ChallengeStatus.WRONG_OBJECT_DETECTED
                }
            }

            if (_wrongAttempts.value >= MAX_WRONG_ATTEMPTS) {
                _challengeStatus.value = ChallengeStatus.MAX_ATTEMPTS_REACHED
            }
        }
    }
}