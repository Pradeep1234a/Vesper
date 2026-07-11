package com.vesper.ledger.ui.savings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vesper.ledger.data.model.SavingsGoal
import com.vesper.ledger.data.repository.SavingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavingsViewModel(
    private val savingsRepository: SavingsRepository
) : ViewModel() {

    val allSavingsGoals: StateFlow<List<SavingsGoal>> = savingsRepository.allSavingsGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSavingsGoal(name: String, targetAmount: Double, targetDateEpochMillis: Long) {
        viewModelScope.launch {
            val goal = SavingsGoal(
                name = name,
                targetAmount = targetAmount,
                currentAmount = 0.0,
                targetDateEpochMillis = targetDateEpochMillis
            )
            savingsRepository.insertSavingsGoal(goal)
        }
    }

    fun deleteSavingsGoal(savingsGoal: SavingsGoal) {
        viewModelScope.launch {
            savingsRepository.deleteSavingsGoal(savingsGoal)
        }
    }

    fun adjustGoalAmount(goal: SavingsGoal, adjustment: Double) {
        viewModelScope.launch {
            val newAmount = (goal.currentAmount + adjustment).coerceAtLeast(0.0)
            val updated = goal.copy(currentAmount = newAmount)
            savingsRepository.updateSavingsGoal(updated)
        }
    }
}

class SavingsViewModelFactory(
    private val savingsRepository: SavingsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SavingsViewModel::class.java)) {
            return SavingsViewModel(savingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
