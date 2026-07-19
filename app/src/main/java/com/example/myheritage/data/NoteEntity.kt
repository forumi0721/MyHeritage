package com.example.myheritage.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    val id: Long = 0,
    
    @ColumnInfo(name = "possess_id")
    val possessId: String,
    
    @ColumnInfo(name = "kind")
    val kind: Int,
    
    @ColumnInfo(name = "type")
    val type: Int?,
    
    @ColumnInfo(name = "label")
    val label: String?,
    
    @ColumnInfo(name = "description")
    val description: String,
    
    @ColumnInfo(name = "is_private")
    val isPrivate: Int,
    
    @ColumnInfo(name = "last_modified")
    val lastModified: Long
)
