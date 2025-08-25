package com.example.projetotcc.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview

val cocoCategories = listOf(
    "person", "backpack", "umbrella", "handbag", "tie", "suitcase",
    "bicycle", "car", "motorcycle", "airplane", "bus", "train", "truck", "boat",
    "traffic light", "fire hydrant", "stop sign", "parking meter", "bench",
    "bird", "cat", "dog", "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe",
    "frisbee", "skis", "snowboard", "sports ball", "skateboard", "surfboard", "tennis racket", "kite",
    "bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana", "apple", "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake",
    "chair", "couch", "potted plant", "bed", "dining table", "toilet", "tv", "laptop", "mouse", "remote", "keyboard", "cell phone", "microwave", "oven", "toaster", "sink", "refrigerator",
    "book", "clock", "vase", "teddy bear", "scissors", "hair drier", "toothbrush"
)

val cocoMainCategories = listOf(
    "person", "vehicle", "outdoor", "animal", "accessory", "sports", "kitchen", "food", "furniture", "electronic", "appliance"
)

val cocoCategoryMap = mapOf(
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

@Composable
fun CategoryScreen(onCategorySelected: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Choose a category",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(cocoMainCategories) { category ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCategorySelected(category) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryScreenPreview() {
    CategoryScreen(onCategorySelected = {})
}
