package kr.stonecold.myheritage.ui.individual

import android.app.AlertDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import kr.stonecold.myheritage.*
import kr.stonecold.myheritage.data.FactEntity
import kr.stonecold.myheritage.data.NoteEntity
import kr.stonecold.myheritage.data.ResourceEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun IndividualDetailScreen(
    personId: Long,
    onItemClick: (NavKey) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as MyHeritageApplication
    val viewModel: IndividualDetailViewModel = viewModel(key = "ind_detail_$personId") {
        IndividualDetailViewModel(app.repository, personId)
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("인물 상세 정보") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "메뉴")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("정보 수정") },
                            onClick = {
                                showMenu = false
                                onItemClick(EditIndividual(personId))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("삭제") },
                            onClick = {
                                showMenu = false
                                viewModel.deleteIndividual {
                                    Toast.makeText(context, "인물이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                    onBackClick()
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("가계도 보기") },
                            onClick = {
                                showMenu = false
                                onItemClick(FamilyTree(personId))
                            }
                        )
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
                IndividualDetailUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is IndividualDetailUiState.Success -> {
                    DetailContent(
                        data = state as IndividualDetailUiState.Success,
                        onItemClick = onItemClick
                    )
                }
                is IndividualDetailUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("오류: ${(state as IndividualDetailUiState.Error).throwable.localizedMessage}")
                    }
                }
            }
        }
    }
}

@Composable
fun DetailContent(
    data: IndividualDetailUiState.Success,
    onItemClick: (NavKey) -> Unit
) {
    val person = data.individual
    val scrollState = rememberScrollState()
    val possessId = "IND_${person.id}"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 프로필 헤더 카드
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val genderIcon = if (person.gender == 1) Icons.Default.Male else Icons.Default.Female
                val genderColor = if (person.gender == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                Icon(
                    imageVector = genderIcon,
                    contentDescription = null,
                    tint = genderColor,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = person.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    if (!person.alias.isNullOrBlank()) {
                        Text(
                            text = "호칭: ${person.alias}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // 가족 관계 요약 카드
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "가족 관계",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (data.childFamily != null) {
                    Text(
                        text = "부모 측 가족 연결됨 (가족 ID: ${data.childFamily.id})",
                        modifier = Modifier.clickable { onItemClick(FamilyDetail(data.childFamily.id)) },
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = "부모 측 가족: 정보 없음",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (data.spouseFamily != null) {
                    Text(
                        text = "배우자 측 가족 연결됨 (가족 ID: ${data.spouseFamily.id})",
                        modifier = Modifier.clickable { onItemClick(FamilyDetail(data.spouseFamily.id)) },
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = "배우자 측 가족: 정보 없음",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 1. 생애 사실 / 기념일 (Facts) 섹션
        SectionHeader(title = "생애 기록 및 기념일", onAddClick = { onItemClick(EditFact(-1L, possessId)) })
        if (data.facts.isEmpty()) {
            Text("등록된 생애 기록이 없습니다.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            data.facts.forEach { fact ->
                FactItem(fact = fact, onEditClick = { onItemClick(EditFact(fact.id, possessId)) })
            }
        }

        // 2. 메모 (Notes) 섹션
        SectionHeader(title = "메모 기록", onAddClick = { onItemClick(EditNote(-1L, possessId)) })
        if (data.notes.isEmpty()) {
            Text("등록된 메모가 없습니다.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            data.notes.forEach { note ->
                NoteItem(note = note, onEditClick = { onItemClick(EditNote(note.id, possessId)) })
            }
        }

        // 3. 미디어 리소스 (Resources) 섹션
        SectionHeader(title = "미디어 파일", onAddClick = { onItemClick(EditResource(-1L, possessId)) })
        if (data.resources.isEmpty()) {
            Text("등록된 미디어 파일이 없습니다.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            data.resources.forEach { resource ->
                ResourceItem(resource = resource, onEditClick = { onItemClick(EditResource(resource.id, possessId)) })
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, onAddClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        IconButton(onClick = onAddClick) {
            Icon(imageVector = Icons.Default.AddCircle, contentDescription = "추가", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun FactItem(fact: FactEntity, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = fact.label ?: "이벤트",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Icon(imageVector = Icons.Default.Edit, contentDescription = "편집", modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            val dateStr = formatLongDate(fact.startDate)
            val calendarTypeStr = if (fact.dateType == 1) " (음력)" else " (양력)"
            Text(text = "날짜: $dateStr$calendarTypeStr", style = MaterialTheme.typography.bodyMedium)
            if (!fact.place.isNullOrBlank()) {
                Text(text = "장소: ${fact.place}", style = MaterialTheme.typography.bodyMedium)
            }
            if (!fact.description.isNullOrBlank()) {
                Text(text = "설명: ${fact.description}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun NoteItem(note: NoteEntity, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = note.label ?: "메모",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Icon(imageVector = Icons.Default.Edit, contentDescription = "편집", modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = note.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun ResourceItem(resource: ResourceEntity, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = resource.label ?: "미디어",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Icon(imageVector = Icons.Default.Edit, contentDescription = "편집", modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "경로: ${resource.uri}", style = MaterialTheme.typography.bodySmall, maxLines = 1)
            if (!resource.description.isNullOrBlank()) {
                Text(text = resource.description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

// 간단한 날짜 변환 함수
fun formatLongDate(dateVal: Long): String {
    if (dateVal <= 0) return "미상"
    return try {
        // 기존 앱의 날짜 포맷은 YYYYMMDD 처럼 저장하는 커스텀 인코딩일 수 있으나
        // 타임스탬프로 유도해서 처리함.
        // YYYYMMDD인지 판단
        if (dateVal in 10000000..99991231) {
            val yr = dateVal / 10000
            val mt = (dateVal % 10000) / 100
            val dy = dateVal % 100
            "${yr}년 ${mt}월 ${dy}일"
        } else {
            val sdf = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA)
            sdf.format(Date(dateVal))
        }
    } catch (e: Exception) {
        dateVal.toString()
    }
}
