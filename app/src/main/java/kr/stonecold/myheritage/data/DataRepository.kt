package kr.stonecold.myheritage.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

interface DataRepository {
    // Individual
    fun getAllIndividuals(): Flow<List<IndividualEntity>>
    suspend fun getAllIndividualsDirect(): List<IndividualEntity>
    suspend fun getIndividualById(id: Long): IndividualEntity?
    fun getIndividualByIdFlow(id: Long): Flow<IndividualEntity?>
    suspend fun getIndividualsByRelativeId(relativeId: Long): List<IndividualEntity>
    suspend fun getIndividualsByRelation(relativeId: Long, relation: Int): List<IndividualEntity>
    suspend fun getIndividualsByFamilyIdChild(familyId: Long): List<IndividualEntity>
    suspend fun getSiblings(familyIdChild: Long, excludeId: Long): List<IndividualEntity>
    suspend fun insertIndividual(individual: IndividualEntity): Long
    suspend fun updateIndividual(individual: IndividualEntity): Int
    suspend fun deleteIndividual(individual: IndividualEntity): Int

    // Family
    fun getAllFamilies(): Flow<List<FamilyEntity>>
    suspend fun getFamilyById(id: Long): FamilyEntity?
    fun getFamilyByIdFlow(id: Long): Flow<FamilyEntity?>
    suspend fun getSpouseFamilies(personId: Long, excludeFamilyId: Long): List<FamilyEntity>
    suspend fun insertFamily(family: FamilyEntity): Long
    suspend fun updateFamily(family: FamilyEntity): Int
    suspend fun deleteFamily(family: FamilyEntity): Int

    // Facts
    fun getAllFactsFlow(): Flow<List<FactEntity>>
    fun getAllNonRepeatedFacts(): Flow<List<FactEntity>>
    suspend fun getFactsByPossessId(possessId: String): List<FactEntity>
    fun getFactsByPossessIdFlow(possessId: String): Flow<List<FactEntity>>
    suspend fun getFactById(id: Long): FactEntity?
    suspend fun getFactByPossessIdAndType(possessId: String, type: Int): FactEntity?
    suspend fun insertFact(fact: FactEntity): Long
    suspend fun updateFact(fact: FactEntity): Int
    suspend fun deleteFact(fact: FactEntity): Int

    // Notes
    fun getNotesByPossessId(possessId: String): Flow<List<NoteEntity>>
    suspend fun getNotesByPossessIdDirect(possessId: String): List<NoteEntity>
    suspend fun getNoteById(id: Long): NoteEntity?
    suspend fun getNotesByPossessIdAndKind(possessId: String, kind: Int): List<NoteEntity>
    suspend fun insertNote(note: NoteEntity): Long
    suspend fun updateNote(note: NoteEntity): Int
    suspend fun deleteNote(note: NoteEntity): Int

    // Resources
    fun getResourcesByPossessId(possessId: String): Flow<List<ResourceEntity>>
    suspend fun getResourceById(id: Long): ResourceEntity?
    suspend fun insertResource(resource: ResourceEntity): Long
    suspend fun updateResource(resource: ResourceEntity): Int
    suspend fun deleteResource(resource: ResourceEntity): Int

    // Database Utilities
    suspend fun clearDatabase()
    fun backupDatabase(): Boolean
    fun restoreDatabase(): Boolean
}

class DefaultDataRepository(private val context: Context) : DataRepository {
    private val database = MyHeritageDatabase.getDatabase(context)
    private val individualDao = database.individualDao()
    private val familyDao = database.familyDao()
    private val factDao = database.factDao()
    private val noteDao = database.noteDao()
    private val resourceDao = database.resourceDao()

    // Individual
    override fun getAllIndividuals(): Flow<List<IndividualEntity>> = individualDao.getAllByName()
    override suspend fun getAllIndividualsDirect(): List<IndividualEntity> = individualDao.getAllByNameDirect()
    override suspend fun getIndividualById(id: Long): IndividualEntity? = individualDao.getById(id)
    override fun getIndividualByIdFlow(id: Long): Flow<IndividualEntity?> = individualDao.getByIdFlow(id)
    override suspend fun getIndividualsByRelativeId(relativeId: Long): List<IndividualEntity> = individualDao.getByRelativeId(relativeId)
    override suspend fun getIndividualsByRelation(relativeId: Long, relation: Int): List<IndividualEntity> = individualDao.getByRelation(relativeId, relation)
    override suspend fun getIndividualsByFamilyIdChild(familyId: Long): List<IndividualEntity> = individualDao.getByFamilyIdChild(familyId)
    override suspend fun getSiblings(familyIdChild: Long, excludeId: Long): List<IndividualEntity> = individualDao.getSiblings(familyIdChild, excludeId)
    override suspend fun insertIndividual(individual: IndividualEntity): Long = individualDao.insert(individual)
    override suspend fun updateIndividual(individual: IndividualEntity): Int = individualDao.update(individual)
    override suspend fun deleteIndividual(individual: IndividualEntity): Int = individualDao.delete(individual)

    // Family
    override fun getAllFamilies(): Flow<List<FamilyEntity>> = familyDao.getAllByKind()
    override suspend fun getFamilyById(id: Long): FamilyEntity? = familyDao.getById(id)
    override fun getFamilyByIdFlow(id: Long): Flow<FamilyEntity?> = familyDao.getByIdFlow(id)
    override suspend fun getSpouseFamilies(personId: Long, excludeFamilyId: Long): List<FamilyEntity> = familyDao.getSpouseFamilies(personId, excludeFamilyId)
    override suspend fun insertFamily(family: FamilyEntity): Long = familyDao.insert(family)
    override suspend fun updateFamily(family: FamilyEntity): Int = familyDao.update(family)
    override suspend fun deleteFamily(family: FamilyEntity): Int = familyDao.delete(family)

    // Facts
    override fun getAllFactsFlow(): Flow<List<FactEntity>> = factDao.getAllFactsFlow()
    override fun getAllNonRepeatedFacts(): Flow<List<FactEntity>> = factDao.getAllNonRepeated()
    override suspend fun getFactsByPossessId(possessId: String): List<FactEntity> = factDao.getByPossessId(possessId)
    override fun getFactsByPossessIdFlow(possessId: String): Flow<List<FactEntity>> = factDao.getByPossessIdFlow(possessId)
    override suspend fun getFactById(id: Long): FactEntity? = factDao.getById(id)
    override suspend fun getFactByPossessIdAndType(possessId: String, type: Int): FactEntity? = factDao.getByPossessIdAndType(possessId, type)
    override suspend fun insertFact(fact: FactEntity): Long = factDao.insert(fact)
    override suspend fun updateFact(fact: FactEntity): Int = factDao.update(fact)
    override suspend fun deleteFact(fact: FactEntity): Int = factDao.delete(fact)

    // Notes
    override fun getNotesByPossessId(possessId: String): Flow<List<NoteEntity>> = noteDao.getByPossessId(possessId)
    override suspend fun getNotesByPossessIdDirect(possessId: String): List<NoteEntity> = noteDao.getByPossessIdDirect(possessId)
    override suspend fun getNoteById(id: Long): NoteEntity? = noteDao.getById(id)
    override suspend fun getNotesByPossessIdAndKind(possessId: String, kind: Int): List<NoteEntity> = noteDao.getByPossessIdAndKind(possessId, kind)
    override suspend fun insertNote(note: NoteEntity): Long = noteDao.insert(note)
    override suspend fun updateNote(note: NoteEntity): Int = noteDao.update(note)
    override suspend fun deleteNote(note: NoteEntity): Int = noteDao.delete(note)

    // Resources
    override fun getResourcesByPossessId(possessId: String): Flow<List<ResourceEntity>> = resourceDao.getByPossessId(possessId)
    override suspend fun getResourceById(id: Long): ResourceEntity? = resourceDao.getById(id)
    override suspend fun insertResource(resource: ResourceEntity): Long = resourceDao.insert(resource)
    override suspend fun updateResource(resource: ResourceEntity): Int = resourceDao.update(resource)
    override suspend fun deleteResource(resource: ResourceEntity): Int = resourceDao.delete(resource)

    // Database Utilities
    override suspend fun clearDatabase() {
        database.clearAllTables()
    }

    override fun backupDatabase(): Boolean {
        val dbFile = context.getDatabasePath("myHeritage.db")
        if (!dbFile.exists()) return false

        val backupDir = context.getExternalFilesDir("backup") ?: return false
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
        val backupFile = File(backupDir, "myHeritage_backup.db")

        return try {
            if (backupFile.exists()) {
                backupFile.delete()
            }
            backupFile.createNewFile()
            copyFile(dbFile, backupFile)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    override fun restoreDatabase(): Boolean {
        val backupDir = context.getExternalFilesDir("backup") ?: return false
        val backupFile = File(backupDir, "myHeritage_backup.db")
        if (!backupFile.exists() || !backupFile.canRead()) return false

        val dbFile = context.getDatabasePath("myHeritage.db")

        return try {
            database.close() // 데이터베이스를 닫아야 정상 덮어쓰기가 됨
            if (dbFile.exists()) {
                dbFile.delete()
            }
            copyFile(backupFile, dbFile)
            // 다시 데이터베이스를 초기화 및 작동 확인을 위해 DB 재오픈은 Room이 알아서 해 줌
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    private fun copyFile(source: File, dest: File) {
        FileInputStream(source).use { inStream ->
            FileOutputStream(dest).use { outStream ->
                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (inStream.read(buffer).also { bytesRead = it } > 0) {
                    outStream.write(buffer, 0, bytesRead)
                }
            }
        }
    }
}
