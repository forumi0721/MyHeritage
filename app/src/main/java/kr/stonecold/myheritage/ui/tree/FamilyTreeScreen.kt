package kr.stonecold.myheritage.ui.tree

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
import kr.stonecold.myheritage.*
import kr.stonecold.myheritage.data.IndividualEntity

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

    // 최대 노드 수에 비례한 가로폭 계산으로 여백 최소화 및 가로 스크롤 범위 확보
    val maxNodesInRow = maxOf(
        if (data.father != null || data.mother != null) 2 else 0,
        data.siblings.size + data.spouses.size + 1,
        data.children.size
    )
    val computedWidth = maxOf(850.dp, (maxNodesInRow * 188).dp)

    // 왼쪽 패딩을 대폭 축소하여 좌측 여백 차단 (32.dp -> 8.dp)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(horizontalScrollState)
            .verticalScroll(verticalScrollState)
            .padding(vertical = 24.dp, horizontal = 8.dp)
    ) {
        val lineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        
        // Canvas 높이: 부모 세대 유무에 맞춰 최적화
        val hasParent = (data.father != null || data.mother != null)
        val canvasHeight = if (hasParent) 450.dp else 300.dp
        
        Canvas(modifier = Modifier.size(computedWidth, canvasHeight)) {
            val xCenter = size.width / 2
            
            // Y축 물리적 좌표 상수 정의
            val yParentMid = 40.dp.toPx()
            val yParentBottom = 80.dp.toPx()
            val ySelfTop = if (hasParent) 160.dp.toPx() else 40.dp.toPx()
            val ySelfBottom = if (hasParent) 240.dp.toPx() else 120.dp.toPx()
            val yChildTop = if (hasParent) 320.dp.toPx() else 200.dp.toPx()

            // 1. 부모 -> 본인 수직 연결선
            if (hasParent) {
                if (data.father != null && data.mother != null) {
                    val xFather = xCenter - 102.dp.toPx()
                    val xMother = xCenter + 102.dp.toPx()
                    // 부모 간 수평선
                    drawLine(
                        color = lineColor,
                        start = Offset(xFather, yParentMid),
                        end = Offset(xMother, yParentMid),
                        strokeWidth = 5f
                    )
                    // 부모 중앙 교차점에서 본인 노드로 향하는 수직선
                    drawLine(
                        color = lineColor,
                        start = Offset(xCenter, yParentMid),
                        end = Offset(xCenter, ySelfTop),
                        strokeWidth = 5f
                    )
                } else {
                    // 한 부모만 존재하는 경우, 그 부모의 밑에서 바로 본인으로 수직선 연결
                    drawLine(
                        color = lineColor,
                        start = Offset(xCenter, yParentBottom),
                        end = Offset(xCenter, ySelfTop),
                        strokeWidth = 5f
                    )
                }
            }

            // 2. 본인 -> 자녀 연결선 (계단식 꺾임 분기선 지원)
            if (data.children.isNotEmpty()) {
                val childCount = data.children.size
                if (childCount == 1) {
                    // 자녀가 1명일 때는 직통 수직선 연결
                    drawLine(
                        color = lineColor,
                        start = Offset(xCenter, ySelfBottom),
                        end = Offset(xCenter, yChildTop),
                        strokeWidth = 5f
                    )
                } else {
                    // 자녀가 2명 이상일 때는 수평 분기선 그리기
                    val pitch = 140.dp.toPx() + 48.dp.toPx()
                    val xFirstChild = xCenter - (childCount - 1) * pitch / 2
                    val yHorizontal = (ySelfBottom + yChildTop) / 2
                    
                    // 본인 노드 바닥에서 수평선까지 수직 하강
                    drawLine(
                        color = lineColor,
                        start = Offset(xCenter, ySelfBottom),
                        end = Offset(xCenter, yHorizontal),
                        strokeWidth = 5f
                    )
                    // 자녀들을 커버하는 가로 수평선
                    drawLine(
                        color = lineColor,
                        start = Offset(xFirstChild, yHorizontal),
                        end = Offset(xFirstChild + (childCount - 1) * pitch, yHorizontal),
                        strokeWidth = 5f
                    )
                    // 수평 분기선에서 각 자녀 노드로 이어지는 수직선들
                    for (i in 0 until childCount) {
                        val xChild = xFirstChild + i * pitch
                        drawLine(
                            color = lineColor,
                            start = Offset(xChild, yHorizontal),
                            end = Offset(xChild, yChildTop),
                            strokeWidth = 5f
                        )
                    }
                }
            }
        }

        // 실제 노드들을 담은 레이아웃
        Column(
            modifier = Modifier.width(computedWidth),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(80.dp)
        ) {
            // 1. 상층: 부모 세대 (부모가 최소 한 명이라도 있을 때만 노출하여 '정보 없음' 박스를 소멸)
            if (hasParent) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(64.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (data.father != null) {
                        TreeNode(person = data.father, label = "부 (아버지)", onClick = { onItemClick(IndividualDetail(data.father.id)) })
                    }

                    if (data.father != null && data.mother != null) {
                        Text("부부", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    if (data.mother != null) {
                        TreeNode(person = data.mother, label = "모 (어머니)", onClick = { onItemClick(IndividualDetail(data.mother.id)) })
                    }
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

            // 3. 하층: 자녀 세대 (자녀가 있을 때만 노출)
            if (data.children.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
    val genderColor = if (person.gender == 1) Color(0xFFE3F2FD) else Color(0xFFFCE4EC)
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
