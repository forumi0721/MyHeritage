package com.example.myheritage.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resources")
data class ResourceEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    val id: Long = 0,
    
    @ColumnInfo(name = "possess_id")
    val possessId: String,
    
    @ColumnInfo(name = "rType")
    val rType: Int,
    
    @ColumnInfo(name = "label")
    val label: String?,
    
    @ColumnInfo(name = "uri")
    val uri: String,
    
    @ColumnInfo(name = "description")
    val description: String?,
    
    @ColumnInfo(name = "last_modified")
    val lastModified: Long
)
