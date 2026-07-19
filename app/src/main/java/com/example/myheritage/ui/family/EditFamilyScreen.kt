package com.example.myheritage.ui.family

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myheritage.*
import com.example.myheritage.data.FamilyEntity
import com.example.myheritage.data.IndividualEntity
import kotlinx.coroutines.launch

@Composable
fun EditFamilyScreen(
    familyId: Long,
    onSuccess: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as MyHeritageApplication
    val repository = app.repository
    val coroutineScope = rememberCoroutineScope()

    var husbandId by remember { mutableStateOf(-1L) }
    var wifeId by remember { mutableStateOf(-1L) }
    var fStatus by remember { mutableStateOf(0) }
    var fKind by remember { mutableStateOf(0) }
    var relativeId by remember { mutableStateOf(-1L) }
    
    var individuals by remember { mutableStateOf<List<IndividualEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(familyId) {
        isLoading = true
        individuals = repository.getAllIndividualsDirect()
        
        if (familyId != -1L) {
            val family = repository.getFamilyById(familyId)
            if (family != null) {
                husbandId = family.husbandId
                wifeId = family.wifeId
                fStatus = family.fStatus
                fKind = family.fKind
                relativeId = family.relativeId
            }
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(if (familyId == -1L) "가족 등록" else "가족 정보 수정") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "뒤로가기")
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
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                var showHusbandMenu by remember { mutableStateOf(false) }
                var showWifeMenu by remember { mutableStateOf(false) }
                var showStatusMenu by remember { mutableStateOf(false) }
                var showKindMenu by remember { mutableStateOf(false) }
                var showRelativeMenu by remember { mutableStateOf(false) }
                
                val statusMap = mapOf(
                    0 to "기혼 (정상)",
                    1 to "사별",
                    2 to "이혼",
                    3 to "사실혼 / 기타"
                )
                
                val kindMap = mapOf(
                    0 to "일반 가족",
                    1 to "본가",
                    2 to "외가",
                    3 to "처가",
                    4 to "친가"
                )

                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("가족 구성원 선택", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    // 남편 선택
                    val selectedHusband = individuals.firstOrNull { it.id == husbandId }
                    Text("남편 (남성 인물)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Box {
                        OutlinedButton(
                            onClick = { showHusbandMenu = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(selectedHusband?.name ?: "남편 인물 선택 (미지정)")
                        }
                        DropdownMenu(
                            expanded = showHusbandMenu,
                            onDismissRequest = { showHusbandMenu = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            DropdownMenuItem(
                                text = { Text("미지정") },
                                onClick = {
                                    husbandId = -1L
                                    showHusbandMenu = false
                                }
                            )
                            individuals.filter { it.gender == 1 }.forEach { person ->
                                DropdownMenuItem(
                                    text = { Text("${person.name} (${person.alias ?: "별칭없음"})") },
                                    onClick = {
                                        husbandId = person.id
                                        showHusbandMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // 아내 선택
                    val selectedWife = individuals.firstOrNull { it.id == wifeId }
                    Text("아내 (여성 인물)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Box {
                        OutlinedButton(
                            onClick = { showWifeMenu = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(selectedWife?.name ?: "아내 인물 선택 (미지정)")
                        }
                        DropdownMenu(
                            expanded = showWifeMenu,
                            onDismissRequest = { showWifeMenu = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            DropdownMenuItem(
                                text = { Text("미지정") },
                                onClick = {
                                    wifeId = -1L
                                    showWifeMenu = false
                                }
                            )
                            individuals.filter { it.gender == 2 }.forEach { person ->
                                DropdownMenuItem(
                                    text = { Text("${person.name} (${person.alias ?: "별칭없음"})") },
                                    onClick = {
                                        wifeId = person.id
                                        showWifeMenu = false
                                    }
                                )
                            }
                        }
                    }

                    HorizontalDivider()
                    Text("가족 상세 속성", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    // 기준 인물(relativeId) 선택
                    val selectedRelative = individuals.firstOrNull { it.id == relativeId }
                    Text("기준 인물 (가족의 주체)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Box {
                        OutlinedButton(
                            onClick = { showRelativeMenu = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(selectedRelative?.name ?: "기준 인물 지정 (선택 사항)")
                        }
                        DropdownMenu(
                            expanded = showRelativeMenu,
                            onDismissRequest = { showRelativeMenu = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            DropdownMenuItem(
                                text = { Text("미지정") },
                                onClick = {
                                    relativeId = -1L
                                    showRelativeMenu = false
                                }
                            )
                            individuals.forEach { person ->
                                DropdownMenuItem(
                                    text = { Text("${person.name} (${person.alias ?: "별칭없음"})") },
                                    onClick = {
                                        relativeId = person.id
                                        showRelativeMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // 가족 상태(fStatus) 선택
                    Text("가족 상태", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Box {
                        OutlinedButton(
                            onClick = { showStatusMenu = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(statusMap[fStatus] ?: "상태 선택")
                        }
                        DropdownMenu(
                            expanded = showStatusMenu,
                            onDismissRequest = { showStatusMenu = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            statusMap.forEach { (statusCode, statusLabel) ->
                                DropdownMenuItem(
                                    text = { Text(statusLabel) },
                                    onClick = {
                                        fStatus = statusCode
                                        showStatusMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // 가족 종류(fKind) 선택
                    Text("가족 유형", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Box {
                        OutlinedButton(
                            onClick = { showKindMenu = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(kindMap[fKind] ?: "유형 선택")
                        }
                        DropdownMenu(
                            expanded = showKindMenu,
                            onDismissRequest = { showKindMenu = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            kindMap.forEach { (kindCode, kindLabel) ->
                                DropdownMenuItem(
                                    text = { Text(kindLabel) },
                                    onClick = {
                                        fKind = kindCode
                                        showKindMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val entity = FamilyEntity(
                                    id = if (familyId == -1L) 0 else familyId,
                                    husbandId = husbandId,
                                    wifeId = wifeId,
                                    fStatus = fStatus,
                                    fKind = fKind,
                                    relativeId = relativeId,
                                    lastModified = System.currentTimeMillis()
                                )
                                if (familyId == -1L) {
                                    val newFamId = repository.insertFamily(entity)
                                    // 본인 정보들의 배우자 가족 ID 자동 업데이트
                                    if (husbandId != -1L) {
                                        val h = repository.getIndividualById(husbandId)
                                        if (h != null) repository.updateIndividual(h.copy(familyIdSpouse = newFamId))
                                    }
                                    if (wifeId != -1L) {
                                        val w = repository.getIndividualById(wifeId)
                                        if (w != null) repository.updateIndividual(w.copy(familyIdSpouse = newFamId))
                                    }
                                } else {
                                    repository.updateFamily(entity)
                                }
                                Toast.makeText(context, "가족 정보가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                                onSuccess()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Done, contentDescription = "저장")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("저장하기", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
