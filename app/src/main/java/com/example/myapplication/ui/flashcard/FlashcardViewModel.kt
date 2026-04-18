package com.example.myapplication.ui.flashcard

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

data class Flashcard(
    val word: String,
    val meaning: String,
    val vietnamese: String,
    val imgKeywords: String
)

data class FlashcardUiState(
    val flashcards: List<Flashcard> = emptyList(),
    val currentIndex: Int = 0,
    val isShuffled: Boolean = false,
    val ttsSpeed: Float = 0.85f
)

class FlashcardViewModel : ViewModel() {

    private val originalList = listOf(
        Flashcard("Admission", "Legal entry into the U.S.", "Sự nhập cảnh", "passport,usa"),
        Flashcard("Allegiance", "Loyalty to a person, country, or cause", "Lòng trung thành", "flag,loyalty"),
        Flashcard("Citizen", "A legally recognized subject of a state", "Công dân", "voter,id"),
        Flashcard("Constitution", "The basic laws of a nation", "Hiến pháp", "document,law"),
        Flashcard("Deported", "Sent out of the country by legal order", "Trục xuất", "airport,police"),
        Flashcard("Eligibility", "The state of being qualified", "Đủ điều kiện", "checklist,success"),
        Flashcard("Naturalization", "Process to become a citizen", "Sự nhập tịch", "ceremony,oath"),
        Flashcard("Oath", "A solemn promise", "Lời thề", "hand,promise"),
        Flashcard("Permanent Resident", "A person who is legal to live forever in the US", "Thường trú nhân", "green-card,home"),
        Flashcard("Register", "To put your name on an official list", "Đăng ký", "pen,list")
    ).sortedBy { it.word }

    private val _uiState = MutableStateFlow(FlashcardUiState(flashcards = originalList))
    val uiState: StateFlow<FlashcardUiState> = _uiState.asStateFlow()

    fun nextCard() {
        _uiState.update { state ->
            val nextIndex = (state.currentIndex + 1) % state.flashcards.size
            state.copy(currentIndex = nextIndex)
        }
    }

    fun prevCard() {
        _uiState.update { state ->
            val prevIndex = if (state.currentIndex > 0) state.currentIndex - 1 else state.flashcards.size - 1
            state.copy(currentIndex = prevIndex)
        }
    }

    fun toggleShuffle() {
        _uiState.update { state ->
            val newIsShuffled = !state.isShuffled
            val newList = if (newIsShuffled) {
                fisherYatesShuffle(originalList)
            } else {
                originalList
            }
            state.copy(
                flashcards = newList,
                isShuffled = newIsShuffled,
                currentIndex = 0 // Reset to first card when shuffling/unshuffling
            )
        }
    }

    fun toggleSpeed() {
        _uiState.update { state ->
            val nextSpeed = when (state.ttsSpeed) {
                0.5f -> 0.85f
                0.85f -> 1.1f
                else -> 0.5f
            }
            state.copy(ttsSpeed = nextSpeed)
        }
    }

    private fun fisherYatesShuffle(list: List<Flashcard>): List<Flashcard> {
        val result = list.toMutableList()
        for (i in result.size - 1 downTo 1) {
            val j = Random.nextInt(i + 1)
            val temp = result[i]
            result[i] = result[j]
            result[j] = temp
        }
        return result
    }
}
