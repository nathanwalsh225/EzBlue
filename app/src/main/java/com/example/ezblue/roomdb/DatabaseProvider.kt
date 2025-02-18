package com.example.ezblue.roomdb

import android.content.Context
import androidx.room.Room

//Code to create the Room DB for the users device
object DatabaseProvider {
    private var INSTANCE: AppDatabase? = null

    fun getRoomDatabase(context: Context): AppDatabase {

        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "ezblue-db"
            ).fallbackToDestructiveMigration()
                .build()
        }
        return INSTANCE!!
    }
}