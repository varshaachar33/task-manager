package com.example.taskmanager.ui

import android.os.Bundle
import androidx.appcompat.widget.SearchView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.R
import com.example.taskmanager.data.Task
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import com.google.android.material.snackbar.Snackbar

class TaskActivity : AppCompatActivity() {

    private val viewModel by viewModels<TaskViewModel>()
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))

        adapter = TaskAdapter(
            onToggle = { task -> viewModel.toggleTask(task) },
            onEdit = { task -> showEditDialog(task) }
        )


        val rv = findViewById<RecyclerView>(R.id.rvTasks)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        rv.itemAnimator?.apply {
            addDuration = 300
            removeDuration = 300
            moveDuration = 300
            changeDuration = 300
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tasks.collect {
                    adapter.submitList(it)
                }
            }
        }

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            showAddDialog()
        }

        findViewById<Chip>(R.id.chipAll).setOnClickListener {
            viewModel.setFilter(TaskViewModel.Filter.ALL)
        }

        findViewById<Chip>(R.id.chipCompleted).setOnClickListener {
            viewModel.setFilter(TaskViewModel.Filter.COMPLETED)
        }

        findViewById<Chip>(R.id.chipPending).setOnClickListener {
            viewModel.setFilter(TaskViewModel.Filter.PENDING)
        }

        val searchView = findViewById<SearchView>(R.id.searchView)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText ?: "")
                return true
            }
        })

        val searchEditText = searchView.findViewById<android.widget.EditText>(
            androidx.appcompat.R.id.search_src_text
        )

        searchEditText?.setTextColor(android.graphics.Color.BLACK)
        searchEditText?.setHintTextColor(android.graphics.Color.GRAY)


        val swipe = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val task = adapter.currentList[position]

                viewModel.deleteTask(task)

                Snackbar.make(rv, "Task deleted", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        viewModel.insertTask(task)   // ← reinsert same object
                    }
                    .show()
            }

        }

        ItemTouchHelper(swipe).attachToRecyclerView(rv)
    }

    private fun showAddDialog() {
        val inputLayout = TextInputLayout(this)
        val editText = TextInputEditText(this)
        editText.hint = "Enter task"
        inputLayout.addView(editText)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Task")
            .setView(inputLayout)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val title = editText.text.toString().trim()
                if (title.isEmpty()) {
                    inputLayout.error = "Task cannot be empty"
                } else {
                    inputLayout.error = null
                    viewModel.addTask(title)
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    private fun showEditDialog(task: Task) {
        val inputLayout = TextInputLayout(this)
        val editText = TextInputEditText(this)
        editText.setText(task.title)
        inputLayout.addView(editText)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Task")
            .setView(inputLayout)
            .setPositiveButton("Update", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val newTitle = editText.text.toString().trim()
                if (newTitle.isEmpty()) {
                    inputLayout.error = "Task cannot be empty"
                } else {
                    inputLayout.error = null
                    viewModel.updateTask(task.copy(title = newTitle))
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }
}
