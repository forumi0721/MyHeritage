package kr.stonecold.myheritage.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface IndividualDao {
    @Query("SELECT * FROM individual ORDER BY name ASC")
    fun getAllByName(): Flow<List<IndividualEntity>>

    @Query("SELECT * FROM individual ORDER BY name ASC")
    suspend fun getAllByNameDirect(): List<IndividualEntity>

    @Query("SELECT * FROM individual WHERE relative_id = :relativeId ORDER BY _id ASC")
    suspend fun getByRelativeId(relativeId: Long): List<IndividualEntity>

    @Query("SELECT * FROM individual WHERE _id = :id LIMIT 1")
    suspend fun getById(id: Long): IndividualEntity?

    @Query("SELECT * FROM individual WHERE _id = :id LIMIT 1")
    fun getByIdFlow(id: Long): Flow<IndividualEntity?>

    @Query("SELECT * FROM individual WHERE family_id_spouse = :familyId ORDER BY _id ASC")
    suspend fun getByFamilyIdSpouse(familyId: Long): List<IndividualEntity>

    @Query("SELECT * FROM individual WHERE family_id_child = :familyId ORDER BY _id ASC")
    suspend fun getByFamilyIdChild(familyId: Long): List<IndividualEntity>

    @Query("SELECT * FROM individual WHERE relative_id = :relativeId AND relation = :relation ORDER BY _id ASC")
    suspend fun getByRelation(relativeId: Long, relation: Int): List<IndividualEntity>

    @Query("SELECT * FROM individual WHERE family_id_child = :familyIdChild AND _id <> :excludeId ORDER BY _id ASC")
    suspend fun getSiblings(familyIdChild: Long, excludeId: Long): List<IndividualEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(individual: IndividualEntity): Long

    @Update
    suspend fun update(individual: IndividualEntity): Int

    @Delete
    suspend fun delete(individual: IndividualEntity): Int

    @Query("DELETE FROM individual")
    suspend fun clearAll()
}

@Dao
interface FamilyDao {
    @Query("SELECT * FROM family ORDER BY fKind ASC")
    fun getAllByKind(): Flow<List<FamilyEntity>>

    @Query("SELECT * FROM family ORDER BY fKind ASC")
    suspend fun getAllByKindDirect(): List<FamilyEntity>

    @Query("SELECT * FROM family WHERE (husband_id = :personId OR wife_id = :personId) AND _id <> :excludeFamilyId ORDER BY fStatus ASC")
    suspend fun getSpouseFamilies(personId: Long, excludeFamilyId: Long): List<FamilyEntity>

    @Query("SELECT * FROM family WHERE _id = :id LIMIT 1")
    suspend fun getById(id: Long): FamilyEntity?

    @Query("SELECT * FROM family WHERE _id = :id LIMIT 1")
    fun getByIdFlow(id: Long): Flow<FamilyEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(family: FamilyEntity): Long

    @Update
    suspend fun update(family: FamilyEntity): Int

    @Delete
    suspend fun delete(family: FamilyEntity): Int

    @Query("DELETE FROM family")
    suspend fun clearAll()
}

@Dao
interface FactDao {
    @Query("SELECT * FROM facts WHERE is_repeated = 0 ORDER BY start_date ASC")
    fun getAllNonRepeated(): Flow<List<FactEntity>>

    @Query("SELECT * FROM facts WHERE is_repeated = 0 ORDER BY start_date ASC")
    suspend fun getAllNonRepeatedDirect(): List<FactEntity>

    @Query("SELECT * FROM facts WHERE possess_id = :possessId AND is_repeated = 0 ORDER BY start_date ASC")
    suspend fun getNonRepeatedByPossessId(possessId: String): List<FactEntity>

    @Query("SELECT * FROM facts WHERE possess_id = :possessId ORDER BY start_date ASC")
    suspend fun getByPossessId(possessId: String): List<FactEntity>

    @Query("SELECT * FROM facts WHERE possess_id = :possessId ORDER BY start_date ASC")
    fun getByPossessIdFlow(possessId: String): Flow<List<FactEntity>>

    @Query("SELECT * FROM facts WHERE _id = :id LIMIT 1")
    suspend fun getById(id: Long): FactEntity?

    @Query("SELECT * FROM facts WHERE possess_id = :possessId AND type = :type ORDER BY is_repeated ASC LIMIT 1")
    suspend fun getByPossessIdAndType(possessId: String, type: Int): FactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fact: FactEntity): Long

    @Update
    suspend fun update(fact: FactEntity): Int

    @Delete
    suspend fun delete(fact: FactEntity): Int

    @Query("DELETE FROM facts")
    suspend fun clearAll()
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE possess_id = :possessId ORDER BY kind ASC")
    fun getByPossessId(possessId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE possess_id = :possessId ORDER BY kind ASC")
    suspend fun getByPossessIdDirect(possessId: String): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE _id = :id LIMIT 1")
    suspend fun getById(id: Long): NoteEntity?

    @Query("SELECT * FROM notes WHERE possess_id = :possessId AND kind = :kind ORDER BY type ASC")
    suspend fun getByPossessIdAndKind(possessId: String, kind: Int): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity): Int

    @Delete
    suspend fun delete(note: NoteEntity): Int

    @Query("DELETE FROM notes")
    suspend fun clearAll()
}

@Dao
interface ResourceDao {
    @Query("SELECT * FROM resources WHERE possess_id = :possessId ORDER BY _id ASC")
    fun getByPossessId(possessId: String): Flow<List<ResourceEntity>>

    @Query("SELECT * FROM resources WHERE possess_id = :possessId ORDER BY _id ASC")
    suspend fun getByPossessIdDirect(possessId: String): List<ResourceEntity>

    @Query("SELECT * FROM resources WHERE _id = :id LIMIT 1")
    suspend fun getById(id: Long): ResourceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(resource: ResourceEntity): Long

    @Update
    suspend fun update(resource: ResourceEntity): Int

    @Delete
    suspend fun delete(resource: ResourceEntity): Int

    @Query("DELETE FROM resources")
    suspend fun clearAll()
}
