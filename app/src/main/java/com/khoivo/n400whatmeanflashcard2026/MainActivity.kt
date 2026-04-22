package com.khoivo.n400whatmeanflashcard2026

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.khoivo.n400whatmeanflashcard2026.ui.flashcard.Flashcard
import com.khoivo.n400whatmeanflashcard2026.ui.flashcard.FlashcardViewModel
import java.util.*

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var isTtsInitialized = mutableStateOf(false)
    private var defaultVoice: Voice? = null
    private var onlineUsVoice: Voice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)

        setContent {
            MaterialTheme {
                FlashcardApp(
                    tts = tts,
                    isTtsReady = isTtsInitialized.value,
                    isOnlineVoiceAvailable = onlineUsVoice != null,
                    onSetPreferOnlineVoice = { preferOnlineVoice ->
                        applyVoicePreference(preferOnlineVoice)
                    }
                )
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
            defaultVoice = tts.voice
            onlineUsVoice = findUsOnlineVoice()
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

    private fun findUsOnlineVoice(): Voice? {
        return tts.voices
            .asSequence()
            .filter { voice ->
                val locale = voice.locale ?: return@filter false
                locale.language == Locale.US.language && locale.country == Locale.US.country
            }
            .filter { voice -> voice.isNetworkConnectionRequired }
            .sortedBy { it.name }
            .firstOrNull { voice ->
                voice.features?.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED) != true
            }
    }

    private fun applyVoicePreference(preferOnlineVoice: Boolean) {
        if (!::tts.isInitialized || !isTtsInitialized.value) {
            return
        }

        tts.language = Locale.US
        val preferredVoice = if (preferOnlineVoice) onlineUsVoice else defaultVoice
        preferredVoice?.let { tts.voice = it }
    }

}

@Composable
fun FlashcardApp(
    tts: TextToSpeech,
    isTtsReady: Boolean,
    isOnlineVoiceAvailable: Boolean,
    onSetPreferOnlineVoice: (Boolean) -> Unit,
    viewModel: FlashcardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentCard = uiState.flashcards.getOrNull(uiState.currentIndex)
    var isWordPickerOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(isTtsReady, uiState.preferOnlineVoice, isOnlineVoiceAvailable) {
        if (isTtsReady) {
            onSetPreferOnlineVoice(uiState.preferOnlineVoice && isOnlineVoiceAvailable)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE0F7FA), Color(0xFFB2EBF2))
                )
            )
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isLandscape = maxWidth > maxHeight
            val isCompactHeight = maxHeight < 760.dp || isLandscape

            if (isLandscape) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .widthIn(min = 240.dp, max = 320.dp)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        tonalElevation = 4.dp
                    ) {
                        TopBar(
                            modifier = Modifier.fillMaxSize(),
                            isCompactHeight = true,
                            isLandscape = true,
                            isShuffled = uiState.isShuffled,
                            ttsSpeed = uiState.ttsSpeed,
                            preferOnlineVoice = uiState.preferOnlineVoice,
                            isOnlineVoiceAvailable = isOnlineVoiceAvailable,
                            selectedWordCount = uiState.selectedWords.size,
                            totalWordCount = uiState.allFlashcards.size,
                            onToggleShuffle = { viewModel.toggleShuffle() },
                            onToggleSpeed = { viewModel.toggleSpeed() },
                            onToggleVoice = { viewModel.toggleVoicePreference() },
                            onInstallVoiceData = {
                                runCatching {
                                    context.startActivity(Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA))
                                }
                            },
                            onOpenWordPicker = { isWordPickerOpen = true }
                        )
                    }

                    DeckPane(
                        modifier = Modifier.weight(1f),
                        currentCard = currentCard,
                        tts = tts,
                        isTtsReady = isTtsReady,
                        ttsSpeed = uiState.ttsSpeed,
                        currentIndex = uiState.currentIndex,
                        totalCards = uiState.flashcards.size,
                        isCompactHeight = true,
                        isLandscape = true,
                        onOpenWordPicker = { isWordPickerOpen = true },
                        onNext = { viewModel.nextCard() },
                        onPrev = { viewModel.prevCard() }
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopBar(
                        modifier = Modifier,
                        isCompactHeight = isCompactHeight,
                        isLandscape = false,
                        isShuffled = uiState.isShuffled,
                        ttsSpeed = uiState.ttsSpeed,
                        preferOnlineVoice = uiState.preferOnlineVoice,
                        isOnlineVoiceAvailable = isOnlineVoiceAvailable,
                        selectedWordCount = uiState.selectedWords.size,
                        totalWordCount = uiState.allFlashcards.size,
                        onToggleShuffle = { viewModel.toggleShuffle() },
                        onToggleSpeed = { viewModel.toggleSpeed() },
                        onToggleVoice = { viewModel.toggleVoicePreference() },
                        onInstallVoiceData = {
                            runCatching {
                                context.startActivity(Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA))
                            }
                        },
                        onOpenWordPicker = { isWordPickerOpen = true }
                    )

                    DeckPane(
                        modifier = Modifier.weight(1f),
                        currentCard = currentCard,
                        tts = tts,
                        isTtsReady = isTtsReady,
                        ttsSpeed = uiState.ttsSpeed,
                        currentIndex = uiState.currentIndex,
                        totalCards = uiState.flashcards.size,
                        isCompactHeight = isCompactHeight,
                        isLandscape = false,
                        onOpenWordPicker = { isWordPickerOpen = true },
                        onNext = { viewModel.nextCard() },
                        onPrev = { viewModel.prevCard() }
                    )
                }
            }
        }

        if (isWordPickerOpen) {
            WordSelectionDialog(
                allFlashcards = uiState.allFlashcards,
                selectedWords = uiState.selectedWords,
                onToggleWord = { viewModel.toggleWordSelection(it) },
                onSelectAll = { viewModel.selectAllWords() },
                onClear = { viewModel.clearSelectedWords() },
                onDismiss = { isWordPickerOpen = false }
            )
        }
    }
}

@Composable
fun DeckPane(
    modifier: Modifier = Modifier,
    currentCard: Flashcard?,
    tts: TextToSpeech,
    isTtsReady: Boolean,
    ttsSpeed: Float,
    currentIndex: Int,
    totalCards: Int,
    isCompactHeight: Boolean,
    isLandscape: Boolean,
    onOpenWordPicker: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (currentCard != null) {
                FlashcardView(
                    card = currentCard,
                    tts = tts,
                    isTtsReady = isTtsReady,
                    ttsSpeed = ttsSpeed,
                    isCompactHeight = isCompactHeight,
                    isLandscape = isLandscape
                )
            } else {
                EmptyDeckMessage(onOpenWordPicker = onOpenWordPicker)
            }
        }

        BottomNavBar(
            currentIndex = currentIndex,
            totalCards = totalCards,
            isCompactHeight = isCompactHeight,
            onNext = onNext,
            onPrev = onPrev
        )
    }
}

@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    isCompactHeight: Boolean,
    isLandscape: Boolean,
    isShuffled: Boolean,
    ttsSpeed: Float,
    preferOnlineVoice: Boolean,
    isOnlineVoiceAvailable: Boolean,
    selectedWordCount: Int,
    totalWordCount: Int,
    onToggleShuffle: () -> Unit,
    onToggleSpeed: () -> Unit,
    onToggleVoice: () -> Unit,
    onInstallVoiceData: () -> Unit,
    onOpenWordPicker: () -> Unit
) {
    val logoSize = if (isCompactHeight) 56.dp else 80.dp
    val topBarPadding = if (isCompactHeight) 12.dp else 16.dp
    val sectionSpacing = if (isCompactHeight) 6.dp else 8.dp
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (isLandscape) Modifier.verticalScroll(scrollState) else Modifier)
            .padding(horizontal = 16.dp, vertical = topBarPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.n400_app_logo),
            contentDescription = "App logo",
            modifier = Modifier
                .size(logoSize)
                .padding(bottom = sectionSpacing),
            contentScale = ContentScale.Fit
        )
        Text(
            text = "N400 WhatMean Flashcard 2026",
            style = if (isCompactHeight) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0277BD),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(sectionSpacing))
        if (isLandscape) {
            OutlinedButton(
                onClick = onToggleSpeed,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Speed: ${if (ttsSpeed == 0.85f) "Normal" else ttsSpeed}",
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(sectionSpacing))
            OutlinedButton(
                onClick = {
                    if (isOnlineVoiceAvailable) {
                        onToggleVoice()
                    } else {
                        onInstallVoiceData()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (isOnlineVoiceAvailable) {
                        "Voice: ${if (preferOnlineVoice) "Online US" else "Default"}"
                    } else {
                        "Get Online Voice"
                    },
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(sectionSpacing))
            OutlinedButton(
                onClick = onToggleShuffle,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isShuffled) "Unshuffle" else "Shuffle")
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onToggleSpeed,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Speed: ${if (ttsSpeed == 0.85f) "Normal" else ttsSpeed}")
                }
                OutlinedButton(
                    onClick = {
                        if (isOnlineVoiceAvailable) {
                            onToggleVoice()
                        } else {
                            onInstallVoiceData()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        if (isOnlineVoiceAvailable) {
                            "Voice: ${if (preferOnlineVoice) "Online US" else "Default"}"
                        } else {
                            "Get Online Voice"
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(sectionSpacing))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
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
        Spacer(modifier = Modifier.height(sectionSpacing))
        OutlinedButton(
            onClick = onOpenWordPicker,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Words: $selectedWordCount / $totalWordCount")
        }
    }
}

@Composable
fun WordSelectionDialog(
    allFlashcards: List<Flashcard>,
    selectedWords: Set<String>,
    onToggleWord: (String) -> Unit,
    onSelectAll: () -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val maxListHeight =
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 280.dp else 420.dp

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Words") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${selectedWords.size} of ${allFlashcards.size} selected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onSelectAll) {
                        Text("Select All")
                    }
                    TextButton(onClick = onClear) {
                        Text("Clear")
                    }
                }
                HorizontalDivider()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = maxListHeight)
                ) {
                    items(allFlashcards, key = { it.word }) { card ->
                        WordSelectionRow(
                            card = card,
                            isSelected = card.word in selectedWords,
                            onToggleWord = onToggleWord
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
fun WordSelectionRow(
    card: Flashcard,
    isSelected: Boolean,
    onToggleWord: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleWord(card.word) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = card.word,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = card.meaning,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun EmptyDeckMessage(onOpenWordPicker: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No words selected",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0277BD),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Choose words to start practicing.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color.DarkGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onOpenWordPicker) {
            Text("Choose Words")
        }
    }
}

@Composable
fun FlashcardView(
    card: Flashcard,
    tts: TextToSpeech,
    isTtsReady: Boolean,
    ttsSpeed: Float,
    isCompactHeight: Boolean,
    isLandscape: Boolean
) {
    var rotated by remember(card) { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (rotated) 180f else 0f,
        animationSpec = tween(durationMillis = 500)
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = if (isCompactHeight) 8.dp else 16.dp),
        contentAlignment = Alignment.Center
    ) {
        val cardAspectRatio = if (isLandscape) 0.92f else 0.7f

        // Size the card from both width and height so it never overlaps the top controls or bottom bar.
        val widthFromScreen = maxWidth * if (isLandscape) 0.78f else if (isCompactHeight) 0.84f else 0.88f
        val widthFromHeight = maxHeight * cardAspectRatio * if (isCompactHeight) 0.9f else 0.94f
        val cardWidth = if (widthFromHeight < widthFromScreen) widthFromHeight else widthFromScreen

        Card(
            modifier = Modifier
                .width(cardWidth)
                .aspectRatio(cardAspectRatio)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                }
                .clickable { rotated = !rotated },
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            if (rotation <= 90f) {
                FlashcardFront(
                    card = card,
                    tts = tts,
                    isTtsReady = isTtsReady,
                    ttsSpeed = ttsSpeed,
                    isCompactHeight = isCompactHeight,
                    isLandscape = isLandscape
                )
            } else {
                Box(
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f }
                ) {
                    FlashcardBack(
                        card = card,
                        tts = tts,
                        isTtsReady = isTtsReady,
                        ttsSpeed = ttsSpeed,
                        isCompactHeight = isCompactHeight,
                        isLandscape = isLandscape
                    )
                }
            }
        }
    }
}

@Composable
fun FlashcardFront(
    card: Flashcard,
    tts: TextToSpeech,
    isTtsReady: Boolean,
    ttsSpeed: Float,
    isCompactHeight: Boolean,
    isLandscape: Boolean
) {
    val context = LocalContext.current
    val imageResId = remember(card.imageName) {
        val id = context.resources.getIdentifier(card.imageName, "drawable", context.packageName)
        if (id != 0) id else R.drawable.ic_launcher_foreground
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val imageHeight = (
            maxHeight * when {
                isLandscape -> 0.26f
                isCompactHeight -> 0.34f
                else -> 0.4f
            }
        ).coerceIn(100.dp, if (isLandscape) 150.dp else 190.dp)

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = "${card.word} image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(if (isCompactHeight) 16.dp else 24.dp))
            Text(
                text = card.word,
                style = when {
                    isLandscape -> MaterialTheme.typography.headlineSmall
                    isCompactHeight -> MaterialTheme.typography.headlineMedium
                    else -> MaterialTheme.typography.headlineLarge
                },
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            PlayAudioButton(
                textToSpeak = card.word,
                tts = tts,
                isTtsReady = isTtsReady,
                speed = ttsSpeed,
                isCompactHeight = isCompactHeight
            )
            Spacer(modifier = Modifier.height(if (isCompactHeight) 16.dp else 24.dp))
        }
    }
}

@Composable
fun FlashcardBack(
    card: Flashcard,
    tts: TextToSpeech,
    isTtsReady: Boolean,
    ttsSpeed: Float,
    isCompactHeight: Boolean,
    isLandscape: Boolean
) {
    val bodyStyle = if (isLandscape) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(if (isCompactHeight) 20.dp else 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("English Meaning", color = Color.Gray, style = MaterialTheme.typography.labelLarge)
        Text(
            text = card.meaning,
            style = bodyStyle,
            color = Color.Red,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(if (isCompactHeight) 24.dp else 32.dp))
        Text("Tiếng Việt", color = Color.Gray, style = MaterialTheme.typography.labelLarge)
        Text(
            text = card.vietnamese,
            style = bodyStyle,
            color = Color.DarkGray,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.weight(1f))
        PlayAudioButton(
            textToSpeak = card.meaning,
            tts = tts,
            isTtsReady = isTtsReady,
            speed = ttsSpeed,
            isCompactHeight = isCompactHeight
        )
    }
}

@Composable
fun PlayAudioButton(
    textToSpeak: String,
    tts: TextToSpeech,
    isTtsReady: Boolean,
    speed: Float,
    isCompactHeight: Boolean
) {
    val buttonSize = if (isCompactHeight) 56.dp else 64.dp

    IconButton(
        onClick = {
            if (isTtsReady) {
                tts.setSpeechRate(speed)
                tts.setPitch(0.9f)
                tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        },
        modifier = Modifier
            .size(buttonSize)
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(buttonSize / 2))
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
    isCompactHeight: Boolean,
    onNext: () -> Unit,
    onPrev: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = if (isCompactHeight) 12.dp else 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev, enabled = totalCards > 0) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous", modifier = Modifier.size(32.dp))
        }
        Text(
            text = if (totalCards > 0) "${currentIndex + 1} / $totalCards" else "0 / 0",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onNext, enabled = totalCards > 0) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", modifier = Modifier.size(32.dp))
        }
    }
}
