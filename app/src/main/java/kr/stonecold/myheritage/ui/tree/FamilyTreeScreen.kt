package kr.stonecold.myheritage.ui.tree

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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

    // 2D 뷰포트 트랜스폼 상태 (화면 전체 줌/이동)
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    var selectedPersonForMenu by remember { mutableStateOf<IndividualEntity?>(null) }

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
                    IconButton(onClick = { 
                        viewModel.loadTreeData()
                        // 새로고침 시 줌 및 화면 위치 원점 복원
                        scale = 1f
                        offsetX = 0f
                        offsetY = 0f
                    }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "새로고침 및 화면 초기화")
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
                    TreeContainer(
                        data = data,
                        scale = scale,
                        offsetX = offsetX,
                        offsetY = offsetY,
                        onTransform = { dScale, dPan ->
                            scale = (scale * dScale).coerceIn(0.4f, 2.5f)
                            offsetX += dPan.x
                            offsetY += dPan.y
                        },
                        onNodeClick = { person ->
                            selectedPersonForMenu = person
                        }
                    )
                }
                is FamilyTreeUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("오류: ${(state as FamilyTreeUiState.Error).throwable.localizedMessage}")
                    }
                }
            }
        }

        // 노드 터치 메뉴 팝업 다이얼로그
        if (selectedPersonForMenu != null) {
            val person = selectedPersonForMenu!!
            AlertDialog(
                onDismissRequest = { selectedPersonForMenu = null },
                title = { Text(text = "${person.name} 님 선택") },
                text = { Text(text = "어떤 가계도 작업을 진행하시겠습니까?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onItemClick(FamilyTree(person.id))
                            selectedPersonForMenu = null
                        }
                    ) {
                        Text("이 인물 기준 가계도 보기")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            onItemClick(IndividualDetail(person.id))
                            selectedPersonForMenu = null
                        }
                    ) {
                        Text("인물 상세 정보 보기")
                    }
                }
            )
        }
    }
}

@Composable
fun TreeContainer(
    data: FamilyTreeUiState.Success,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    onTransform: (Float, Offset) -> Unit,
    onNodeClick: (IndividualEntity) -> Unit
) {
    val hasParent = (data.father != null || data.mother != null)
    
    // 세대별 노드 수에 따른 가로폭 계산 (93dp 노드 크기 기준 최적화)
    val maxNodesInRow = maxOf(
        if (data.father != null && data.mother != null) 2 else if (hasParent) 1 else 0,
        data.siblings.size + data.spouses.size + 1,
        data.children.size
    )
    val computedWidth = maxOf(750.dp, (maxNodesInRow * 140).dp)
    val computedHeight = if (hasParent) 550.dp else 380.dp

    // 1D 네이티브 스크롤을 걷어내어 드래그(Pan)와 줌(Zoom) 충돌을 방지하고 자유 2D 이동 구현
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    onTransform(zoom, pan)
                }
            }
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(computedWidth, computedHeight)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY,
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f)
                )
        ) {
            val lineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            
            // 정밀 수치 기반 Canvas 연결선 그리기 (93dp x 53dp 노드 핏 적용)
            Canvas(modifier = Modifier.size(computedWidth, computedHeight)) {
                val xCenter = size.width / 2

                // 물리 좌표 상수 정의 (그룹 박스 두께와 패딩 적용값)
                val yParentMid = 38.5.dp.toPx() // 노드 절반 26.5dp + 박스 패딩 12dp
                val yParentBottom = 77.dp.toPx() // 노드 53dp + 박스 패딩 12dp*2
                
                val ySelfTop = if (hasParent) 177.dp.toPx() // 부모 77dp + 간격 100dp
                else 40.dp.toPx()
                val ySelfSpouseTop = if (hasParent) 193.dp.toPx() // ySelfTop + 세대패딩 16dp
                else 56.dp.toPx()
                val ySelfSpouseBottom = if (hasParent) 270.dp.toPx() // ySelfSpouseTop + 노드 53dp + 부부패딩 12dp*2
                else 133.dp.toPx()
                val ySelfBottom = if (hasParent) 286.dp.toPx() // ySelfSpouseBottom + 세대패딩 16dp
                else 149.dp.toPx()
                
                val yChildTop = if (hasParent) 386.dp.toPx() // 중층하단 286dp + 간격 100dp
                else 249.dp.toPx()
                val yChildNodeTop = if (hasParent) 402.dp.toPx() // yChildTop + 세대패딩 16dp
                else 265.dp.toPx()

                // 1. 부모 부부 -> 본인 부부 연결선
                if (hasParent) {
                    if (data.father != null && data.mother != null) {
                        val xFather = xCenter - 58.5.dp.toPx() // 노드 절반 46.5dp + spacedBy 절반 12dp
                        val xMother = xCenter + 58.5.dp.toPx()
                        
                        // 부모 부부 연결선 (수평)
                        drawLine(
                            color = lineColor,
                            start = Offset(xFather, yParentMid),
                            end = Offset(xMother, yParentMid),
                            strokeWidth = 5f
                        )
                        // 부모 교차점에서 중층 본인 부부 박스 상단으로 내리는 수직선
                        drawLine(
                            color = lineColor,
                            start = Offset(xCenter, yParentMid),
                            end = Offset(xCenter, ySelfTop),
                            strokeWidth = 5f
                        )
                    } else {
                        // 편부모인 경우 해당 단독 부모 하단에서 본인 부부 박스로 수직선 연결
                        drawLine(
                            color = lineColor,
                            start = Offset(xCenter, yParentBottom),
                            end = Offset(xCenter, ySelfTop),
                            strokeWidth = 5f
                        )
                    }
                }

                // 2. 본인 부부 -> 자녀 세대 연결선 (계단식 꺾임 분기선)
                if (data.children.isNotEmpty()) {
                    val childCount = data.children.size
                    val hasSpouse = data.spouses.isNotEmpty()
                    
                    val ySelfSpouseMid = (ySelfSpouseTop + ySelfSpouseBottom) / 2
                    val yLineStart = if (hasSpouse) ySelfSpouseMid else ySelfBottom
                    val xLineStart = xCenter

                    // 배우자가 있는 경우 본인과 배우자를 잇는 수평선 그리기
                    if (hasSpouse) {
                        val xSelf = xCenter - 58.5.dp.toPx()
                        val xSpouse = xCenter + 58.5.dp.toPx()
                        drawLine(
                            color = lineColor,
                            start = Offset(xSelf, ySelfSpouseMid),
                            end = Offset(xSpouse, ySelfSpouseMid),
                            strokeWidth = 5f
                        )
                    }

                    if (childCount == 1) {
                        // 자녀 1명 직통 수직선
                        drawLine(
                            color = lineColor,
                            start = Offset(xLineStart, yLineStart),
                            end = Offset(xCenter, yChildTop),
                            strokeWidth = 5f
                        )
                    } else {
                        // 자녀 2명 이상 계단식 분기선 그리기 (93dp 노드 적용)
                        val pitch = 93.dp.toPx() + 24.dp.toPx()
                        val xFirstChild = xCenter - (childCount - 1) * pitch / 2
                        val yHorizontal = (yLineStart + yChildTop) / 2
                        
                        // 본인 부부 수평선/바닥에서 수평 분기선까지 하강 수직선
                        drawLine(
                            color = lineColor,
                            start = Offset(xLineStart, yLineStart),
                            end = Offset(xCenter, yHorizontal),
                            strokeWidth = 5f
                        )
                        // 가로 분기 수평선
                        drawLine(
                            color = lineColor,
                            start = Offset(xFirstChild, yHorizontal),
                            end = Offset(xFirstChild + (childCount - 1) * pitch, yHorizontal),
                            strokeWidth = 5f
                        )
                        // 수평선에서 각 자녀 노드로 하강하는 수직선들
                        for (i in 0 until childCount) {
                            val xChild = xFirstChild + i * pitch
                            drawLine(
                                color = lineColor,
                                start = Offset(xChild, yHorizontal),
                                end = Offset(xChild, yChildNodeTop),
                                strokeWidth = 5f
                            )
                        }
                    }
                }
            }

            // 실제 노드 및 그룹핑 아웃라인 박스 레이아웃 (실측 크기 유지)
            Column(
                modifier = Modifier.width(computedWidth),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(100.dp)
            ) {
                // 1. 상층: 부모 세대 (부모가 한 명이라도 존재하면 노출)
                if (hasParent) {
                    if (data.father != null && data.mother != null) {
                        // 부부 그룹 외부 테두리 박스 감싸기
                        Box(
                            modifier = Modifier
                                .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TreeNode(person = data.father, label = "부 (아버지)", onClick = { onNodeClick(data.father) })
                                TreeNode(person = data.mother, label = "모 (어머니)", onClick = { onNodeClick(data.mother) })
                            }
                        }
                    } else if (data.father != null) {
                        TreeNode(person = data.father, label = "부 (아버지)", onClick = { onNodeClick(data.father) })
                    } else if (data.mother != null) {
                        TreeNode(person = data.mother, label = "모 (어머니)", onClick = { onNodeClick(data.mother) })
                    }
                }

                // 2. 중층: 형제, 자매 및 본인 부부 통합 세대 박스
                Box(
                    modifier = Modifier
                        .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 형제들 (왼쪽)
                        data.siblings.filter { it.gender == 1 }.forEach { sibling ->
                            TreeNode(person = sibling, label = "형제", onClick = { onNodeClick(sibling) })
                        }

                        // 본인 부부 결합 그룹 박스
                        Box(
                            modifier = Modifier
                                .border(2.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TreeNode(
                                    person = data.self,
                                    label = "본인",
                                    isRoot = true,
                                    onClick = { onNodeClick(data.self) }
                                )
                                data.spouses.forEach { spouse ->
                                    TreeNode(
                                        person = spouse,
                                        label = "배우자",
                                        onClick = { onNodeClick(spouse) }
                                    )
                                }
                            }
                        }

                        // 자매들 (오른쪽)
                        data.siblings.filter { it.gender == 2 }.forEach { sibling ->
                            TreeNode(person = sibling, label = "자매", onClick = { onNodeClick(sibling) })
                        }
                    }
                }

                // 3. 하층: 자녀 세대 박스
                if (data.children.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            data.children.forEach { child ->
                                TreeNode(
                                    person = child,
                                    label = "자녀",
                                    onClick = { onNodeClick(child) }
                                )
                            }
                        }
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
    val borderWidth = if (isRoot) 2.dp else 1.dp

    Box(
        modifier = Modifier
            .requiredSize(93.dp, 53.dp) // 카드 뭉개짐(찌그러짐)을 원천 방지하기 위해 requiredSize 지정
            .background(genderColor, shape = RoundedCornerShape(6.dp))
            .border(borderWidth, borderColor, shape = RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = person.name,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp, // 축소된 크기에 맞게 가나다 가독 비율 축소
                color = Color.Black,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = label,
                fontSize = 8.sp,
                color = Color.DarkGray,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            if (!person.alias.isNullOrBlank()) {
                Text(
                    text = "(${person.alias})",
                    fontSize = 8.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
        }
    }
}
