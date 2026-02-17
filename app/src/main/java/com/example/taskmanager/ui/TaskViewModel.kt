package com.example.taskmanager.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.data.Task
import com.example.taskmanager.data.TaskDatabase
import com.example.taskmanager.data.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    enum class Filter { ALL, COMPLETED, PENDING }

    private val repository: TaskRepository
    private val savedStateHandle = SavedStateHandle()

    private val _filter = savedStateHandle.getStateFlow("filter", Filter.ALL)
    private val _searchQuery = MutableStateFlow("")

    val tasks: StateFlow<List<Task>>

    init {
        val dao = TaskDatabase.getInstance(application).taskDao()
        repository = TaskRepository(dao)

        // 🔥 Sync Firestore → Room on app launch
        viewModelScope.launch {
            repository.syncFromFirestore()
        }

        tasks = combine(_filter, _searchQuery) { filter, query ->
            Pair(filter, query)
        }.flatMapLatest { (filter, query) ->

            val source = when (filter) {
                Filter.ALL -> repository.allTasks
                Filter.COMPLETED -> repository.completedTasks
                Filter.PENDING -> repository.pendingTasks
            }

            source.map { list ->
                if (query.isBlank()) {
                    list
                } else {
                    list.filter {
                        it.title.contains(query, ignoreCase = true)
                    }
                }
            }

        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    fun setFilter(filter: Filter) {
        savedStateHandle["filter"] = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addTask(title: String) = viewModelScope.launch {
        val task = Task(
            id = UUID.randomUUID().toString(),
            title = title
        )
        repository.insert(task)
    }

    fun updateTask(task: Task) = viewModelScope.launch {
        repository.update(task)
    }

    fun toggleTask(task: Task) = viewModelScope.launch {
        repository.update(task.copy(isCompleted = !task.isCompleted))
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        repository.delete(task)
    }

    fun insertTask(task: Task) = viewModelScope.launch {
        repository.insert(task)
    }
}
