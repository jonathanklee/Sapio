package com.klee.sapio.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [EvaluationEntity::class, IconEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun evaluationDao(): EvaluationDao
    abstract fun iconDao(): IconDao
}
