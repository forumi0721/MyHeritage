package kr.stonecold.myheritage

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import kr.stonecold.myheritage.ui.main.MainScreen
import kr.stonecold.myheritage.ui.individual.IndividualListScreen
import kr.stonecold.myheritage.ui.individual.IndividualDetailScreen
import kr.stonecold.myheritage.ui.individual.EditIndividualScreen
import kr.stonecold.myheritage.ui.family.FamilyListScreen
import kr.stonecold.myheritage.ui.family.FamilyDetailScreen
import kr.stonecold.myheritage.ui.family.EditFamilyScreen
import kr.stonecold.myheritage.ui.anniversary.AnniversaryListScreen
import kr.stonecold.myheritage.ui.anniversary.EditFactScreen
import kr.stonecold.myheritage.ui.note.EditNoteScreen
import kr.stonecold.myheritage.ui.resource.EditResourceScreen
import kr.stonecold.myheritage.ui.tree.FamilyTreeScreen
import kr.stonecold.myheritage.ui.settings.SettingsScreen

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(Main)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Main> {
          MainScreen(onItemClick = { navKey -> backStack.add(navKey) })
        }
        entry<IndividualList> {
          IndividualListScreen(
            onItemClick = { navKey -> backStack.add(navKey) },
            onBackClick = { backStack.removeLastOrNull() }
          )
        }
        entry<FamilyList> {
          FamilyListScreen(
            onItemClick = { navKey -> backStack.add(navKey) },
            onBackClick = { backStack.removeLastOrNull() }
          )
        }
        entry<AnniversaryList> {
          AnniversaryListScreen(
            onItemClick = { navKey -> backStack.add(navKey) },
            onBackClick = { backStack.removeLastOrNull() }
          )
        }
        entry<IndividualDetail> { key ->
          IndividualDetailScreen(
            personId = key.id,
            onItemClick = { navKey -> backStack.add(navKey) },
            onBackClick = { backStack.removeLastOrNull() }
          )
        }
        entry<FamilyDetail> { key ->
          FamilyDetailScreen(
            familyId = key.id,
            onItemClick = { navKey -> backStack.add(navKey) },
            onBackClick = { backStack.removeLastOrNull() }
          )
        }
        entry<EditIndividual> { key ->
          EditIndividualScreen(
            personId = key.id,
            onSuccess = { backStack.removeLastOrNull() },
            onBackClick = { backStack.removeLastOrNull() }
          )
        }
        entry<EditFamily> { key ->
          EditFamilyScreen(
            familyId = key.id,
            onSuccess = { backStack.removeLastOrNull() },
            onBackClick = { backStack.removeLastOrNull() }
          )
        }
        entry<EditFact> { key ->
          EditFactScreen(
            factId = key.id,
            possessId = key.possessId,
            onSuccess = { backStack.removeLastOrNull() },
            onBackClick = { backStack.removeLastOrNull() }
          )
        }
        entry<EditNote> { key ->
          EditNoteScreen(
            noteId = key.id,
            possessId = key.possessId,
            onSuccess = { backStack.removeLastOrNull() },
            onBackClick = { backStack.removeLastOrNull() }
          )
        }
        entry<EditResource> { key ->
          EditResourceScreen(
            resourceId = key.id,
            possessId = key.possessId,
            onSuccess = { backStack.removeLastOrNull() },
            onBackClick = { backStack.removeLastOrNull() }
          )
        }
        entry<FamilyTree> { key ->
          FamilyTreeScreen(
            rootIndividualId = key.rootIndividualId,
            onItemClick = { navKey -> backStack.add(navKey) },
            onBackClick = { backStack.removeLastOrNull() }
          )
        }
        entry<Settings> {
          SettingsScreen(
            onBackClick = { backStack.removeLastOrNull() }
          )
        }
      },
  )
}
