package com.example.khatma

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map

data class JuzUiState(
    val juzNumber: Int,
    val progressPercent: Int,
    val isCompleted: Boolean,
    val statusLabel: String
)

class KhatmaViewModel(private val repository: KhatmaRepository) : ViewModel() {
    val juzList = repository.observeAllJuz()
        .map { list ->
            (1..JuzPageMapping.TOTAL_JUZ).map { juzNum ->
                val record = list.find { it.juzNumber == juzNum }
                val percent = record?.progressPercent ?: 0
                val completed = record?.isCompleted ?: false
                JuzUiState(
                        juzNumber = juzNum,
                        progressPercent = percent,
                        isCompleted = completed,
                        statusLabel = when {
                            completed -> "مكتمل ✓"
                            percent == 0 -> "لم يبدأ"
                            else -> "قيد القراءة $percent%"
                        }
                    )
                }
            }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onPageRead(page: Int) {
        viewModelScope.launch { repository.updateProgress(page) }
    }

    suspend fun openJuz(juz: Int): Int {
        val record = repository.getJuz(juz)
        return if (record?.isCompleted == true) {
            val nextJuz = if (juz < JuzPageMapping.TOTAL_JUZ) juz + 1 else 1
            JuzPageMapping.startPage(nextJuz)
        } else if (record != null && record.lastReadPage > 0) {
            record.lastReadPage
        } else {
            JuzPageMapping.startPage(juz)
        }
    }

    fun resetKhatma() {
        viewModelScope.launch { repository.startNewKhatma() }
    }

    fun resetJuz(juz: Int) {
        viewModelScope.launch { repository.resetJuz(juz) }
    }
}
