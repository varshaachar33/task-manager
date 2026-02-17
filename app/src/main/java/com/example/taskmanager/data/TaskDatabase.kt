package com.example.taskmanager.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Task::class], version = 3)
abstract class TaskDatabase: RoomDatabase(){
    abstract fun taskDao(): TaskDao

    companion object{

        @Volatile //handling threads
        private var INSTANCE: TaskDatabase? = null

        fun getInstance(context: Context): TaskDatabase{ //There must be only one Room DB per app process
            return INSTANCE?: synchronized(this){ //If INSTANCE exists → return it immediately , Else → enter synchronized block
                Room.databaseBuilder(
                    context.applicationContext, //Avoids Activity memory leaks
                    TaskDatabase::class.java, //Database schema definition
                    "task_database" //File name on device storage
                )
                 .fallbackToDestructiveMigration()
                 .build()
                 .also { INSTANCE = it } //Stores DB instance , Ensures singleton behavior
            }
        }
    }
}

//App asks for DB
//If DB exists → return it
//If not:
//Lock thread
//Build DB
//Save instance
//Return DB