package kr.stonecold.myheritage.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family")
data class FamilyEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    val id: Long = 0,
    
    @ColumnInfo(name = "husband_id")
    val husbandId: Long = -1,
    
    @ColumnInfo(name = "wife_id")
    val wifeId: Long = -1,
    
    @ColumnInfo(name = "fStatus")
    val fStatus: Int = 0,
    
    @ColumnInfo(name = "fKind")
    val fKind: Int = 0,
    
    @ColumnInfo(name = "relative_id")
    val relativeId: Long = -1,
    
    @ColumnInfo(name = "last_modified")
    val lastModified: Long
)
