package kr.stonecold.myheritage

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object Main : NavKey

@Serializable
data object IndividualList : NavKey

@Serializable
data object FamilyList : NavKey

@Serializable
data object AnniversaryList : NavKey

@Serializable
data class IndividualDetail(val id: Long) : NavKey

@Serializable
data class FamilyDetail(val id: Long) : NavKey

@Serializable
data class EditIndividual(val id: Long = -1) : NavKey

@Serializable
data class EditFamily(val id: Long = -1) : NavKey

@Serializable
data class EditFact(val id: Long = -1, val possessId: String) : NavKey

@Serializable
data class EditNote(val id: Long = -1, val possessId: String) : NavKey

@Serializable
data class EditResource(val id: Long = -1, val possessId: String) : NavKey

@Serializable
data class FamilyTree(val rootIndividualId: Long) : NavKey

@Serializable
data object Settings : NavKey
