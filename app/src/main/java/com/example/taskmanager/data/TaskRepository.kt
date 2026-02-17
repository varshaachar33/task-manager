package com.example.taskmanager.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TaskRepository(private val dao: TaskDao) {

    private val firestore = FirebaseFirestore.getInstance()
    private val taskCollection = firestore.collection("tasks")

    // -------------------------
    // ROOM FLOWS
    // -------------------------
    val allTasks = dao.getAllTasks()
    val completedTasks = dao.getCompletedTasks()
    val pendingTasks = dao.getPendingTasks()

    // -------------------------
    // INSERT
    // -------------------------
    suspend fun insert(task: Task) {
        dao.insert(task)

        taskCollection
            .document(task.id)
            .set(task)
            .await()
    }

    // -------------------------
    // UPDATE
    // -------------------------
    suspend fun update(task: Task) {
        dao.update(task)

        taskCollection
            .document(task.id)
            .set(task)
            .await()
    }

    // -------------------------
    // DELETE
    // -------------------------
    suspend fun delete(task: Task) {
        dao.delete(task)

        taskCollection
            .document(task.id)
            .delete()
            .await()
    }

    // -------------------------
    // 🔥 SYNC FROM FIRESTORE
    // -------------------------
    suspend fun syncFromFirestore() {
        try {
            val snapshot = taskCollection.get().await()

            for (doc in snapshot.documents) {
                val task = doc.toObject(Task::class.java)
                if (task != null) {
                    dao.insert(task) // REPLACE prevents duplicates
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
