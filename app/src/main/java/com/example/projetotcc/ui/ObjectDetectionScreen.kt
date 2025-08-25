package com.example.projetotcc.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projetotcc.R
import com.example.projetotcc.viewmodel.ChallengeStatus
import com.example.projetotcc.viewmodel.ObjectDetectionViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


data class DetectionResult(val boundingBox: RectF, val text: String)

@Composable
fun ObjectDetectionScreen(
    category: String,
    onBack: () -> Unit = {},
    onObjectDetected: (String) -> Unit,
    viewModel: ObjectDetectionViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Passa a categoria para o ViewModel
    LaunchedEffect(category) {
        viewModel.setCategory(category)
    }

    val bitmap by viewModel.bitmap.collectAsState()
    val detectionResults by viewModel.detectionResults.collectAsState()
    val detectedLabel by viewModel.detectedLabel.collectAsState()

    val objectToFind by viewModel.objectToFind.collectAsState()
    val challengeStatus by viewModel.challengeStatus.collectAsState()
    val wrongAttempts by viewModel.wrongAttempts.collectAsState()


    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && viewModel.currentPhotoPath.isNotEmpty()) {
                try {
                    val capturedBitmap = getCapturedImage(viewModel.currentPhotoPath)
                    viewModel.onPictureTaken(context, capturedBitmap)
                } catch (e: Exception) {
                    scope.launch { snackbarHostState.showSnackbar("Error processing image.") }
                }
            } else {
                scope.launch { snackbarHostState.showSnackbar("Failed to capture image.") }
            }
        }
    )

    fun dispatchTakePictureIntent() {
        try {
            val photoFile = createImageFile(context).apply {
                viewModel.currentPhotoPath = absolutePath
            }
            val photoURI: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            takePictureLauncher.launch(photoURI)
        } catch (e: Exception) {
            scope.launch { snackbarHostState.showSnackbar("Error preparing camera.") }
        }
    }

    ObjectDetectionScreenContent(
        bitmap = bitmap,
        detectionResults = detectionResults,
        detectedLabel = detectedLabel,
        onTakePictureClick = { dispatchTakePictureIntent() },
        onBackClick = onBack,
        onNextClick = { onObjectDetected(it) },
        snackbarHostState = snackbarHostState,
        objectToFind = objectToFind,
        challengeStatus = challengeStatus,
        onTryAgainClick = viewModel::tryAgain,
        onNewChallengeClick = viewModel::startNewChallenge,
        wrongAttempts = wrongAttempts,
        scope = scope
    )
}

private fun createImageFile(context: Context): File {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
        "JPEG_${timeStamp}_",
        ".jpg",
        storageDir
    )
}

private fun getCapturedImage(photoPath: String): Bitmap {
    val options = BitmapFactory.Options().apply { inMutable = true }
    val bitmap = BitmapFactory.decodeFile(photoPath, options)
    val exif = ExifInterface(photoPath)
    val orientation = exif.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
    }
    return createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

@Composable
fun ImageWithBoundingBoxes(bitmap: Bitmap, detectionResults: List<DetectionResult>) {
    Box(contentAlignment = Alignment.Center) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Imagem Capturada",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawIntoCanvas { canvas ->
                val scaleFactorX = size.width / bitmap.width
                val scaleFactorY = size.height / bitmap.height
                val scaleFactor = kotlin.math.min(scaleFactorX, scaleFactorY)
                val offsetX = (size.width - bitmap.width * scaleFactor) / 2
                val offsetY = (size.height - bitmap.height * scaleFactor) / 2

                detectionResults.forEach { result ->
                    val box = result.boundingBox
                    val scaledBox = RectF(
                        box.left * scaleFactor + offsetX,
                        box.top * scaleFactor + offsetY,
                        box.right * scaleFactor + offsetX,
                        box.bottom * scaleFactor + offsetY
                    )

                    // VVVVVV  COMENTE ESTE BLOCO PARA REMOVER O BOUNDING BOX VVVVVV
                    canvas.nativeCanvas.drawRect(
                        scaledBox,
                        Paint().apply {
                            color = Color.RED
                            style = Paint.Style.STROKE
                            strokeWidth = 8f
                        }
                    )
                    // ^^^^^^  FIM DO BLOCO A SER COMENTADO  ^^^^^^

                    canvas.nativeCanvas.drawText(
                        result.text,
                        scaledBox.left,
                        scaledBox.top - 10,
                        Paint().apply {
                            color = Color.YELLOW
                            textSize = 50f
                            style = Paint.Style.FILL
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun ObjectDetectionScreenContent(
    bitmap: Bitmap?,
    detectionResults: List<DetectionResult>,
    detectedLabel: String,
    onTakePictureClick: () -> Unit,
    onBackClick: () -> Unit,
    onNextClick: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    objectToFind: String,
    challengeStatus: ChallengeStatus,
    onTryAgainClick: () -> Unit,
    onNewChallengeClick: () -> Unit,
    wrongAttempts: Int,
    scope: CoroutineScope // <-- Adicionado
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        ImageWithBoundingBoxes(bitmap = bitmap, detectionResults = detectionResults)
                    } else {
                        Text(
                            text = "Take a photo to detect objects",
                            fontSize = 22.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        val maxLives = 3
                        val remainingLives = maxLives - wrongAttempts
                        repeat(remainingLives) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = "Remaining Life",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        repeat(wrongAttempts) {
                            Icon(
                                imageVector = Icons.Outlined.FavoriteBorder,
                                contentDescription = "Lost Life",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Button(
                        onClick = {
                            onNewChallengeClick()
                            scope.launch {
                                snackbarHostState.showSnackbar("Novo objeto sorteado! Tente identificar outro objeto da mesma categoria.")
                            }
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh, // alterado para ícone de recarregar
                            contentDescription = "Recarregar objeto",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AnimatedVisibility(visible = challengeStatus != ChallengeStatus.MAX_ATTEMPTS_REACHED) {
                        Text(
                            text = "Find a: ${objectToFind.replaceFirstChar { it.uppercase() }}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }
                    when (challengeStatus) {
                        ChallengeStatus.CORRECT_OBJECT_DETECTED -> Text("You got it!", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        ChallengeStatus.WRONG_OBJECT_DETECTED -> Text("Wrong! Detected object: ${detectedLabel.split(",").first()}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        ChallengeStatus.NO_OBJECT_DETECTED -> Text("No object found, try again!", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        ChallengeStatus.MAX_ATTEMPTS_REACHED -> Text("You missed 3 times! Let's try a new word.", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
                        else -> {}
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (challengeStatus == ChallengeStatus.AWAITING_USER_ACTION) {
                    Button(onClick = onTakePictureClick, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                        Icon(painter = painterResource(id = R.drawable.ic_camera), contentDescription = "Camera Icon", modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Take Photo", fontSize = 16.sp)
                    }
                }
                if (challengeStatus == ChallengeStatus.WRONG_OBJECT_DETECTED || challengeStatus == ChallengeStatus.NO_OBJECT_DETECTED) {
                    Button(onClick = onTryAgainClick, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                        Text(text = "Try Again", fontSize = 16.sp)
                    }
                }
                if (challengeStatus == ChallengeStatus.MAX_ATTEMPTS_REACHED) {
                    Button(onClick = onNewChallengeClick, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                        Text(text = "Next Challenge", fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (challengeStatus != ChallengeStatus.MAX_ATTEMPTS_REACHED) {
                        Button(onClick = onBackClick, modifier = Modifier.weight(1f).height(50.dp)) {
                            Text(text = "Back")
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    if (challengeStatus == ChallengeStatus.CORRECT_OBJECT_DETECTED) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { onNextClick(detectedLabel) }, modifier = Modifier.weight(1f).height(50.dp)) {
                            Text(text = "Next")
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Preview(showBackground = true, name = "Detection Screen - No Image")
@Composable
fun ObjectDetectionScreenPreview_NoImage() {
    val scope = rememberCoroutineScope()
    ObjectDetectionScreenContent(
        bitmap = null,
        detectionResults = emptyList(),
        detectedLabel = "Point to an object",
        objectToFind = "Cell phone",
        challengeStatus = ChallengeStatus.AWAITING_USER_ACTION,
        onTakePictureClick = {},
        onBackClick = {},
        onNextClick = {},
        onTryAgainClick = {},
        onNewChallengeClick = {},
        wrongAttempts = 0,
        snackbarHostState = remember { SnackbarHostState() },
        scope = scope
    )
}

@Preview(showBackground = true, name = "Detection Screen - With Detection")
@Composable
fun ObjectDetectionScreenPreview_WithDetection() {
    val previewBitmap = createBitmap(100, 100, Bitmap.Config.ARGB_8888)
    val scope = rememberCoroutineScope()
    ObjectDetectionScreenContent(
        bitmap = previewBitmap,
        detectionResults = listOf(DetectionResult(RectF(10f, 10f, 90f, 90f), "Cell phone")),
        detectedLabel = "Cell phone detected",
        objectToFind = "Cell phone",
        challengeStatus = ChallengeStatus.CORRECT_OBJECT_DETECTED,
        onTakePictureClick = {},
        onBackClick = {},
        onNextClick = {},
        onTryAgainClick = {},
        onNewChallengeClick = {},
        wrongAttempts = 0,
        snackbarHostState = remember { SnackbarHostState() },
        scope = scope
    )
}

@Preview(showBackground = true, name = "Detection Screen - With Image, No Detection")
@Composable
fun ObjectDetectionScreenPreview_WithImageNoDetection() {
    val previewBitmap = createBitmap(100, 100, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(previewBitmap)
    val paint = Paint().apply { color = Color.LTGRAY }
    canvas.drawRect(0f, 0f, 100f, 100f, paint)
    val scope = rememberCoroutineScope()
    ObjectDetectionScreenContent(
        bitmap = previewBitmap,
        detectionResults = emptyList(),
        detectedLabel = "No object identified",
        objectToFind = "Cell phone",
        challengeStatus = ChallengeStatus.AWAITING_USER_ACTION,
        onTakePictureClick = {},
        onBackClick = {},
        onNextClick = {},
        onTryAgainClick = {},
        onNewChallengeClick = {},
        wrongAttempts = 1,
        snackbarHostState = remember { SnackbarHostState() },
        scope = scope
    )
}