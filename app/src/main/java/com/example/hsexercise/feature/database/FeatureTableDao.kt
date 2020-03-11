package com.example.hsexercise.feature.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Dao
interface FeatureTableDao {
    @Query("SELECT * FROM feature WHERE page = :pageNumber")
    fun getAllForPage(pageNumber: Int): List<FeatureModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(models: List<FeatureModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(featureModel: FeatureModel)
}

@Entity(tableName = "feature")
data class FeatureModel(
    @PrimaryKey
    val id: String,
    val author: String,
    val width: Int,
    val height: Int,
    var url: String,
    var page: Int? = null
)
