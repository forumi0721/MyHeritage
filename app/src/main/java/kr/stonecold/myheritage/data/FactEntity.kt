package kr.stonecold.myheritage.data

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
    val isRepeated: Int, // 0: true (매년반복), 1: false
    
    @ColumnInfo(name = "show_in_list", defaultValue = "1")
    val showInList: Int = 1, // 1: 목록에 출력, 0: 목록 출력 안함
    
    @ColumnInfo(name = "place")
    val place: String?,
    
    @ColumnInfo(name = "description")
    val description: String?,
    
    @ColumnInfo(name = "last_modified")
    val lastModified: Long
)
