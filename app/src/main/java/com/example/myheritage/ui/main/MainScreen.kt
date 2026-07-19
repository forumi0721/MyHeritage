package com.example.myheritage.ui.main

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.myheritage.*
import com.example.myheritage.theme.MyHeritageTheme

@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val app = context.applicationContext as MyHeritageApplication
    val viewModel: MainScreenViewModel = viewModel {
        MainScreenViewModel(app.repository)
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("My Heritages", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (state) {
                MainScreenUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is MainScreenUiState.Success -> {
                    DashboardContent(
                        data = (state as MainScreenUiState.Success).data,
                        onItemClick = onItemClick
                    )
                }
                is MainScreenUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "데이터를 불러오는 중 오류가 발생했습니다: ${(state as MainScreenUiState.Error).throwable.localizedMessage}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardContent(
    data: DashboardData,
    onItemClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 통계 대시보드 카드
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "가계 통계",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatItem(label = "등록된 인물", count = data.totalIndividuals)
                    StatItem(label = "등록된 가족", count = data.totalFamilies)
                    StatItem(label = "등록된 기념일", count = data.totalAnniversaries)
                }
            }
        }

        Text(
            text = "바로가기 메뉴",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        // 메뉴 항목 모델 정의
        val menuItems = listOf(
            MenuItem(
                title = "인물 목록",
                icon = Icons.Default.Person,
                onClick = { onItemClick(IndividualList) }
            ),
            MenuItem(
                title = "가족 목록",
                icon = Icons.Default.Home,
                onClick = { onItemClick(FamilyList) }
            ),
            MenuItem(
                title = "기념일 목록",
                icon = Icons.Default.DateRange,
                onClick = { onItemClick(AnniversaryList) }
            ),
            MenuItem(
                title = "나의 프로필",
                icon = Icons.Default.AccountCircle,
                onClick = {
                    if (data.rootIndividualId != -1L) {
                        onItemClick(IndividualDetail(data.rootIndividualId))
                    } else {
                        Toast.makeText(context, "먼저 인물 목록에서 인물을 추가해주세요.", Toast.LENGTH_SHORT).show()
                        onItemClick(EditIndividual(-1L))
                    }
                }
            ),
            MenuItem(
                title = "나의 가족",
                icon = Icons.Default.Face,
                onClick = {
                    if (data.myFamilyId != -1L) {
                        onItemClick(FamilyDetail(data.myFamilyId))
                    } else {
                        Toast.makeText(context, "연결된 가족이 없습니다. 가족을 등록해주세요.", Toast.LENGTH_SHORT).show()
                        onItemClick(EditFamily(-1L))
                    }
                }
            ),
            MenuItem(
                title = "가계도 보기",
                icon = Icons.Default.Share, // 가계도 대체 아이콘
                onClick = {
                    if (data.rootIndividualId != -1L) {
                        onItemClick(FamilyTree(data.rootIndividualId))
                    } else {
                        Toast.makeText(context, "가계도를 생성할 인물이 없습니다. 인물을 등록해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
            ),
            MenuItem(
                title = "백업 & 설정",
                icon = Icons.Default.Settings,
                onClick = { onItemClick(Settings) }
            )
        )

        // 메뉴 그리드 레이아웃
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(menuItems) { item ->
                MenuGridItem(item)
            }
        }
    }
}

@Composable
fun StatItem(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
        )
    }
}

data class MenuItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun MenuGridItem(item: MenuItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { item.onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
