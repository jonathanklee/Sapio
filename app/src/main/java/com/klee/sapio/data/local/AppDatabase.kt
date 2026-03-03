package com.klee.sapio.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [EvaluationEntity::class, IconEntity::class, DeviceAppEntity::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun evaluationDao(): EvaluationDao
    abstract fun iconDao(): IconDao
    abstract fun deviceAppDao(): DeviceAppDao
}
