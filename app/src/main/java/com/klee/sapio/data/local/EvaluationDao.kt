package com.klee.sapio.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EvaluationDao {

    @Query(
        """
        SELECT * FROM EvaluationEntity
        ORDER BY updatedAt DESC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun listLatestEvaluations(
        limit: Int,
        offset: Int
    ): List<EvaluationEntity>

    @Query(
        """
        SELECT * FROM EvaluationEntity
        WHERE (name LIKE :pattern OR packageName LIKE :pattern)
        ORDER BY name
        """
    )
    suspend fun searchEvaluations(pattern: String): List<EvaluationEntity>

    @Query(
        """
        SELECT * FROM EvaluationEntity
        WHERE packageName = :packageName AND microg = :microg AND secure = :secure
        LIMIT 1
        """
    )
    suspend fun getEvaluation(
        packageName: String,
        microg: Int,
        secure: Int
    ): EvaluationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<EvaluationEntity>)
}

@Dao
interface IconDao {

    @Query(
        """
        SELECT * FROM IconEntity
        WHERE name = :iconName
        ORDER BY id DESC
        """
    )
    suspend fun findByName(iconName: String): List<IconEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<IconEntity>)

    @Query("DELETE FROM IconEntity WHERE id = :id")
    suspend fun deleteById(id: Int)
}
