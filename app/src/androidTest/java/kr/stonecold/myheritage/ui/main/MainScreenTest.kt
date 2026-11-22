package kr.stonecold.myheritage.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kr.stonecold.myheritage.IndividualList
import kr.stonecold.myheritage.theme.MyHeritageTheme
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** UI tests for the main dashboard. */
class MainScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private val navigationEvents = mutableListOf<androidx.navigation3.runtime.NavKey>()
  private val dashboardData = DashboardData(
    totalIndividuals = 2,
    totalFamilies = 1,
    totalAnniversaries = 3,
    rootIndividualId = 1,
    myFamilyId = 1,
  )

  @Before
  fun setup() {
    navigationEvents.clear()
    composeTestRule.setContent {
      MyHeritageTheme {
        DashboardContent(data = dashboardData, onItemClick = navigationEvents::add)
      }
    }
  }

  @Test
  fun dashboardShowsStatisticsAndMenu() {
    composeTestRule.onNodeWithText("가계 통계").assertExists()
    composeTestRule.onNodeWithText("등록된 인물").assertExists()
    composeTestRule.onNodeWithText("등록된 가족").assertExists()
    composeTestRule.onNodeWithText("등록된 기념일").assertExists()
    composeTestRule.onNodeWithText("인물 목록").assertExists()
  }

  @Test
  fun tappingIndividualListEmitsNavigationEvent() {
    composeTestRule.onNodeWithText("인물 목록").performClick()
    assertEquals(listOf(IndividualList), navigationEvents)
  }
}
