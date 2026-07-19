package kr.stonecold.myheritage.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "individual")
data class IndividualEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    val id: Long = 0,
    
    @ColumnInfo(name = "gender")
    val gender: Int,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "alias")
    val alias: String?,
    
    @ColumnInfo(name = "contact_id")
    val contactId: Long = -1,
    
    @ColumnInfo(name = "relation")
    val relation: Int = 0,
    
    @ColumnInfo(name = "relative_id")
    val relativeId: Long = -1,
    
    @ColumnInfo(name = "kind")
    val kind: Int = 0,
    
    @ColumnInfo(name = "last_modified")
    val lastModified: Long,
    
    @ColumnInfo(name = "family_id_spouse")
    val familyIdSpouse: Long = -1,
    
    @ColumnInfo(name = "family_id_child")
    val familyIdChild: Long = -1
)
