package com.example.myheritage.ui.tree

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.myheritage.*
import com.example.myheritage.data.IndividualEntity

@Composable
fun FamilyTreeScreen(
    rootIndividualId: Long,
    onItemClick: (NavKey) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as MyHeritageApplication
    val viewModel: FamilyTreeViewModel = viewModel(key = "tree_$rootIndividualId") {
        FamilyTreeViewModel(app.repository, rootIndividualId)
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("가계도 트리") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadTreeData() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "새로고침")
                    }
                },
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
                FamilyTreeUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is FamilyTreeUiState.Success -> {
                    val data = state as FamilyTreeUiState.Success
                    TreeContainer(data = data, onItemClick = onItemClick)
                }
                is FamilyTreeUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("오류: ${(state as FamilyTreeUiState.Error).throwable.localizedMessage}")
                    }
                }
            }
        }
    }
}

@Composable
fun TreeContainer(
    data: FamilyTreeUiState.Success,
    onItemClick: (NavKey) -> Unit
) {
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    // 2D 스크롤이 가능하도록 설계
    Box(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(horizontalScrollState)
            .verticalScroll(verticalScrollState)
            .padding(32.dp)
    ) {
        // 백그라운드 연결선 그리기
        // 노드들의 위치를 고려해 선을 그림
        val lineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        
        Canvas(modifier = Modifier.size(1000.dp, 800.dp)) {
            // 1. 부모 -> 본인 연결선 (상층에서 중층)
            // 부모 연합 노드가 X=500, Y=120 부근에 있고 본인 노드가 X=500, Y=300 부근에 있다고 가정
            drawLine(
                color = lineColor,
                start = Offset(x = 500f, y = 140f),
                end = Offset(x = 500f, y = 280f),
                strokeWidth = 4f
            )
            // 2. 부모 -> 형제자매 연결선 (수평 가지선)
            if (data.siblings.isNotEmpty()) {
                drawLine(
                    color = lineColor,
                    start = Offset(x = 300f, y = 200f),
                    end = Offset(x = 700f, y = 200f),
                    strokeWidth = 4f
                )
            }
            // 3. 본인 -> 자녀 연결선 (중층에서 하층)
            if (data.children.isNotEmpty()) {
                drawLine(
                    color = lineColor,
                    start = Offset(x = 500f, y = 380f),
                    end = Offset(x = 500f, y = 520f),
                    strokeWidth = 4f
                )
            }
        }

        Column(
            modifier = Modifier.width(1000.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(80.dp)
        ) {
            // 1. 상층: 부모 세대
            Row(
                horizontalArrangement = Arrangement.spacedBy(64.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (data.father != null) {
                    TreeNode(person = data.father, label = "부 (아버지)", onClick = { onItemClick(IndividualDetail(data.father.id)) })
                } else {
                    PlaceholderNode("아버지 정보 없음")
                }

                Text("부부", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)

                if (data.mother != null) {
                    TreeNode(person = data.mother, label = "모 (어머니)", onClick = { onItemClick(IndividualDetail(data.mother.id)) })
                } else {
                    PlaceholderNode("어머니 정보 없음")
                }
            }

            // 2. 중층: 본인, 배우자 및 형제자매
            Row(
                horizontalArrangement = Arrangement.spacedBy(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 형제자매들 (왼쪽)
                data.siblings.filter { it.gender == 1 }.forEach { sibling ->
                    TreeNode(person = sibling, label = "형제", onClick = { onItemClick(IndividualDetail(sibling.id)) })
                }

                // 본인 노드
                TreeNode(
                    person = data.self,
                    label = "본인",
                    isRoot = true,
                    onClick = { onItemClick(IndividualDetail(data.self.id)) }
                )

                // 배우자 노드들
                data.spouses.forEach { spouse ->
                    TreeNode(
                        person = spouse,
                        label = "배우자",
                        onClick = { onItemClick(IndividualDetail(spouse.id)) }
                    )
                }

                // 자매들 (오른쪽)
                data.siblings.filter { it.gender == 2 }.forEach { sibling ->
                    TreeNode(person = sibling, label = "자매", onClick = { onItemClick(IndividualDetail(sibling.id)) })
                }
            }

            // 3. 하층: 자녀 세대
            Row(
                horizontalArrangement = Arrangement.spacedBy(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (data.children.isEmpty()) {
                    Text("등록된 자녀가 없습니다.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    data.children.forEach { child ->
                        TreeNode(
                            person = child,
                            label = "자녀",
                            onClick = { onItemClick(IndividualDetail(child.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TreeNode(
    person: IndividualEntity,
    label: String,
    isRoot: Boolean = false,
    onClick: () -> Unit
) {
    val genderColor = if (person.gender == 1) Color(0xFFC7E2F8) else Color(0xFFFCDDEC)
    val borderColor = if (isRoot) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val borderWidth = if (isRoot) 3.dp else 1.dp

    Box(
        modifier = Modifier
            .width(140.dp)
            .height(80.dp)
            .background(genderColor, shape = RoundedCornerShape(8.dp))
            .border(borderWidth, borderColor, shape = RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = person.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color.DarkGray,
                fontWeight = FontWeight.SemiBold
            )
            if (!person.alias.isNullOrBlank()) {
                Text(
                    text = "(${person.alias})",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun PlaceholderNode(text: String) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .height(80.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
