package kr.stonecold.myheritage.ui.main

import kr.stonecold.myheritage.data.*
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MainScreenViewModelTest {
  @Test
  fun uiState_initiallyLoading() = runTest {
    val viewModel = MainScreenViewModel(FakeMyModelRepository())
    assertEquals(viewModel.uiState.value, MainScreenUiState.Loading)
  }
}

private class FakeMyModelRepository : DataRepository {
    override fun getAllIndividuals(): Flow<List<IndividualEntity>> = flow { emit(emptyList()) }
    override suspend fun getAllIndividualsDirect(): List<IndividualEntity> = emptyList()
    override suspend fun getIndividualById(id: Long): IndividualEntity? = null
    override fun getIndividualByIdFlow(id: Long): Flow<IndividualEntity?> = flow { emit(null) }
    override suspend fun getIndividualsByRelativeId(relativeId: Long): List<IndividualEntity> = emptyList()
    override suspend fun getIndividualsByRelation(relativeId: Long, relation: Int): List<IndividualEntity> = emptyList()
    override suspend fun getIndividualsByFamilyIdChild(familyId: Long): List<IndividualEntity> = emptyList()
    override suspend fun getSiblings(familyIdChild: Long, excludeId: Long): List<IndividualEntity> = emptyList()
    override suspend fun insertIndividual(individual: IndividualEntity): Long = 0L
    override suspend fun updateIndividual(individual: IndividualEntity): Int = 0
    override suspend fun deleteIndividual(individual: IndividualEntity): Int = 0

    override fun getAllFamilies(): Flow<List<FamilyEntity>> = flow { emit(emptyList()) }
    override suspend fun getFamilyById(id: Long): FamilyEntity? = null
    override fun getFamilyByIdFlow(id: Long): Flow<FamilyEntity?> = flow { emit(null) }
    override suspend fun getSpouseFamilies(personId: Long, excludeFamilyId: Long): List<FamilyEntity> = emptyList()
    override suspend fun insertFamily(family: FamilyEntity): Long = 0L
    override suspend fun updateFamily(family: FamilyEntity): Int = 0
    override suspend fun deleteFamily(family: FamilyEntity): Int = 0

    override fun getAllNonRepeatedFacts(): Flow<List<FactEntity>> = flow { emit(emptyList()) }
    override suspend fun getFactsByPossessId(possessId: String): List<FactEntity> = emptyList()
    override fun getFactsByPossessIdFlow(possessId: String): Flow<List<FactEntity>> = flow { emit(emptyList()) }
    override suspend fun getFactById(id: Long): FactEntity? = null
    override suspend fun getFactByPossessIdAndType(possessId: String, type: Int): FactEntity? = null
    override suspend fun insertFact(fact: FactEntity): Long = 0L
    override suspend fun updateFact(fact: FactEntity): Int = 0
    override suspend fun deleteFact(fact: FactEntity): Int = 0

    override fun getNotesByPossessId(possessId: String): Flow<List<NoteEntity>> = flow { emit(emptyList()) }
    override suspend fun getNotesByPossessIdDirect(possessId: String): List<NoteEntity> = emptyList()
    override suspend fun getNoteById(id: Long): NoteEntity? = null
    override suspend fun getNotesByPossessIdAndKind(possessId: String, kind: Int): List<NoteEntity> = emptyList()
    override suspend fun insertNote(note: NoteEntity): Long = 0L
    override suspend fun updateNote(note: NoteEntity): Int = 0
    override suspend fun deleteNote(note: NoteEntity): Int = 0

    override fun getResourcesByPossessId(possessId: String): Flow<List<ResourceEntity>> = flow { emit(emptyList()) }
    override suspend fun getResourceById(id: Long): ResourceEntity? = null
    override suspend fun insertResource(resource: ResourceEntity): Long = 0L
    override suspend fun updateResource(resource: ResourceEntity): Int = 0
    override suspend fun deleteResource(resource: ResourceEntity): Int = 0

    override suspend fun clearDatabase() {}
    override fun backupDatabase(): Boolean = true
    override fun restoreDatabase(): Boolean = true
}
