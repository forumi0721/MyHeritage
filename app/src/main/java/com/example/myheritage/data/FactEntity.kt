package com.example.myheritage.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "facts")
data class FactEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    val id: Long = 0,
    
    @ColumnInfo(name = "possess_id")
    val possessId: String,
    
    @ColumnInfo(name = "type")
    val type: Int,
    
    @ColumnInfo(name = "label")
    val label: String?,
    
    @ColumnInfo(name = "start_date")
    val startDate: Long,
    
    @ColumnInfo(name = "date_type")
    val dateType: Int,
    
    @ColumnInfo(name = "is_repeated")
    val isRepeated: Int, // 0: true, 1: false (기존 앱 매핑 유지)
    
    @ColumnInfo(name = "place")
    val place: String?,
    
    @ColumnInfo(name = "description")
    val description: String?,
    
    @ColumnInfo(name = "last_modified")
    val lastModified: Long
)
