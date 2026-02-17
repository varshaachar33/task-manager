package com.example.taskmanager.ui

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.R
import com.example.taskmanager.data.Task


class TaskAdapter(
    private val onToggle: (Task) -> Unit,
    private val onEdit: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskVH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(oldItem: Task, newItem: Task) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Task, newItem: Task) =
                oldItem == newItem
        }
    }

    inner class TaskVH(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvTitle)
        val checkBox: CheckBox = view.findViewById(R.id.cbDone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskVH(view)
    }

    override fun onBindViewHolder(holder: TaskVH, position: Int) {
        val task = getItem(position)

        holder.title.text = task.title

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = task.isCompleted

        if (task.isCompleted) {
            holder.title.paintFlags =
                holder.title.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.title.paintFlags =
                holder.title.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        holder.checkBox.setOnCheckedChangeListener { _, _ ->
            onToggle(task)
        }

        holder.itemView.setOnClickListener {
            onEdit(task)
        }
    }
}
