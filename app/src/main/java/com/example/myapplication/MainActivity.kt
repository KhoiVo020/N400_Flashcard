package com.example.myapplication

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myapplication.ui.flashcard.Flashcard
import com.example.myapplication.ui.flashcard.FlashcardViewModel
import java.util.*

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var isTtsInitialized = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)

        setContent {
            MaterialTheme {
                FlashcardApp(tts, isTtsInitialized.value)
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
            
            // Try to find a male voice
            val maleVoice = tts.voices.find { voice ->
                val name = voice.name.lowercase()
                name.contains("male") || name.contains("david") || name.contains("alex")
            }
            maleVoice?.let { tts.voice = it }
            
            isTtsInitialized.value = true
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}

@Composable
fun FlashcardApp(tts: TextToSpeech, isTtsReady: Boolean, viewModel: FlashcardViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val currentCard = uiState.flashcards.getOrNull(uiState.currentIndex)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE0F7FA), Color(0xFFB2EBF2))
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                isShuffled = uiState.isShuffled,
                ttsSpeed = uiState.ttsSpeed,
                onToggleShuffle = { viewModel.toggleShuffle() },
                onToggleSpeed = { viewModel.toggleSpeed() }
            )

            currentCard?.let { card ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    FlashcardView(
                        card = card,
                        index = uiState.currentIndex,
                        tts = tts,
                        isTtsReady = isTtsReady,
                        ttsSpeed = uiState.ttsSpeed
                    )
                }
            }

            BottomNavBar(
                currentIndex = uiState.currentIndex,
                totalCards = uiState.flashcards.size,
                onNext = { viewModel.nextCard() },
                onPrev = { viewModel.prevCard() }
            )
        }
    }
}

@Composable
fun TopBar(
    isShuffled: Boolean,
    ttsSpeed: Float,
    onToggleShuffle: () -> Unit,
    onToggleSpeed: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🇺🇸 N400 Flashcards",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0277BD)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(onClick = onToggleSpeed) {
                Text("Speed: ${if (ttsSpeed == 0.85f) "Normal" else ttsSpeed}")
            }
            OutlinedButton(onClick = onToggleShuffle) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isShuffled) "Unshuffle" else "Shuffle")
            }
        }
    }
}

@Composable
fun FlashcardView(
    card: Flashcard,
    index: Int,
    tts: TextToSpeech,
    isTtsReady: Boolean,
    ttsSpeed: Float
) {
    var rotated by remember(card) { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (rotated) 180f else 0f,
        animationSpec = tween(durationMillis = 500)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .aspectRatio(0.7f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable { rotated = !rotated },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        if (rotation <= 90f) {
            // Front Side
            FlashcardFront(card, index, tts, isTtsReady, ttsSpeed)
        } else {
            // Back Side
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
            ) {
                FlashcardBack(card, tts, isTtsReady, ttsSpeed)
            }
        }
    }
}

@Composable
fun FlashcardFront(
    card: Flashcard,
    index: Int,
    tts: TextToSpeech,
    isTtsReady: Boolean,
    ttsSpeed: Float
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = "https://loremflickr.com/400/300/${card.imgKeywords}?lock=$index",
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = card.word,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        PlayAudioButton(textToSpeak = card.word, tts = tts, isTtsReady = isTtsReady, speed = ttsSpeed)
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun FlashcardBack(
    card: Flashcard,
    tts: TextToSpeech,
    isTtsReady: Boolean,
    ttsSpeed: Float
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("English Meaning", color = Color.Gray, style = MaterialTheme.typography.labelLarge)
        Text(
            text = card.meaning,
            style = MaterialTheme.typography.titleLarge,
            color = Color.Red,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text("Tiếng Việt", color = Color.Gray, style = MaterialTheme.typography.labelLarge)
        Text(
            text = card.vietnamese,
            style = MaterialTheme.typography.titleLarge,
            color = Color.DarkGray,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.weight(1f))
        PlayAudioButton(textToSpeak = card.meaning, tts = tts, isTtsReady = isTtsReady, speed = ttsSpeed)
    }
}

@Composable
fun PlayAudioButton(textToSpeak: String, tts: TextToSpeech, isTtsReady: Boolean, speed: Float) {
    IconButton(
        onClick = {
            if (isTtsReady) {
                tts.setSpeechRate(speed)
                tts.setPitch(0.9f)
                tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        },
        modifier = Modifier
            .size(64.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(32.dp))
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play Audio",
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun BottomNavBar(
    currentIndex: Int,
    totalCards: Int,
    onNext: () -> Unit,
    onPrev: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Previous", modifier = Modifier.size(32.dp))
        }
        Text(
            text = "${currentIndex + 1} / $totalCards",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Default.ArrowForward, contentDescription = "Next", modifier = Modifier.size(32.dp))
        }
    }
}
