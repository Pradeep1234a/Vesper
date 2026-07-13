package com.vesper.ledger.ui.category

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class CategorySortOption {
    CUSTOM,
    NAME_ASC,
    NAME_DESC
}

class CategoryViewModel(
    application: Application,
    private val transactionRepository: TransactionRepository
) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("vesper_category_prefs", Context.MODE_PRIVATE)

    val searchQuery = MutableStateFlow("")
    val selectedType = MutableStateFlow(TransactionType.EXPENSE)
    val sortBy = MutableStateFlow(CategorySortOption.CUSTOM)

    // Load custom order from SharedPreferences
    private fun getCustomOrder(type: TransactionType): List<Long> {
        val key = if (type == TransactionType.INCOME) "order_income" else "order_expense"
        val raw = sharedPrefs.getString(key, "") ?: ""
        if (raw.isEmpty()) return emptyList()
        return raw.split(",").mapNotNull { it.toLongOrNull() }
    }

    private fun saveCustomOrder(type: TransactionType, order: List<Long>) {
        val key = if (type == TransactionType.INCOME) "order_income" else "order_expense"
        sharedPrefs.edit().putString(key, order.joinToString(",")).apply()
    }

    // Combine repository categories flow with search, type filter, and sort option
    val categories: StateFlow<List<Category>> = combine(
        transactionRepository.allCategories,
        searchQuery,
        selectedType,
        sortBy
    ) { allCats, query, type, sort ->
        // 1. Filter by Type and Search query
        val filtered = allCats.filter { cat ->
            cat.type == type && (query.isEmpty() || cat.name.contains(query, ignoreCase = true))
        }

        // 2. Apply Sorting
        when (sort) {
            CategorySortOption.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
            CategorySortOption.NAME_DESC -> filtered.sortedByDescending { it.name.lowercase() }
            CategorySortOption.CUSTOM -> {
                val customOrder = getCustomOrder(type)
                if (customOrder.isEmpty()) {
                    // Default to sorting by ID if no custom order saved yet
                    filtered.sortedBy { it.id }
                } else {
                    // Sort according to saved order, append any new categories to the end
                    filtered.sortedWith { c1, c2 ->
                        val idx1 = customOrder.indexOf(c1.id)
                        val idx2 = customOrder.indexOf(c2.id)
                        when {
                            idx1 != -1 && idx2 != -1 -> idx1.compareTo(idx2)
                            idx1 != -1 -> -1
                            idx2 != -1 -> 1
                            else -> c1.id.compareTo(c2.id)
                        }
                    }
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun moveCategory(category: Category, up: Boolean) {
        viewModelScope.launch {
            val currentList = categories.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == category.id }
            if (index == -1) return@launch

            val targetIndex = if (up) index - 1 else index + 1
            if (targetIndex in currentList.indices) {
                // Swap in the list
                val temp = currentList[index]
                currentList[index] = currentList[targetIndex]
                currentList[targetIndex] = temp

                // Save new custom order to preferences
                saveCustomOrder(selectedType.value, currentList.map { it.id })
                // Trigger state refresh by mutating sort or forcing combined flows to re-evaluate
                sortBy.value = CategorySortOption.CUSTOM
            }
        }
    }

    fun addCategory(name: String, iconName: String, type: TransactionType, colorHex: String) {
        viewModelScope.launch {
            val newCat = Category(
                name = name,
                iconName = iconName,
                type = type,
                colorHex = colorHex
            )
            transactionRepository.insertCategory(newCat)
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            transactionRepository.updateCategory(category)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            transactionRepository.deleteCategory(category)
        }
    }

    fun getCategory(id: Long, onResult: (Category?) -> Unit) {
        viewModelScope.launch {
            val cat = transactionRepository.getCategoryById(id)
            onResult(cat)
        }
    }
}

class CategoryViewModelFactory(
    private val application: Application,
    private val transactionRepository: TransactionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
            return CategoryViewModel(application, transactionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
