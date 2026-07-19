package kr.stonecold.myheritage.ui.individual

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import kr.stonecold.myheritage.*
import kr.stonecold.myheritage.data.FactEntity

@Composable
fun EditIndividualScreen(
    personId: Long,
    onSuccess: () -> Unit,
    onBackClick: () -> Unit,
    onItemClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as MyHeritageApplication
    val viewModel: EditIndividualViewModel = viewModel(key = "edit_ind_$personId") {
        EditIndividualViewModel(app.repository, personId)
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(if (personId == -1L) "인물 등록" else "인물 정보 수정") },
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
            when (state) {
                EditIndividualUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is EditIndividualUiState.Success -> {
                    val successData = state as EditIndividualUiState.Success
                    EditForm(
                        data = successData,
                        personId = personId,
                        onItemClick = onItemClick,
                        onSave = { name, alias, gender, relativeId, relation, birthDateCode, birthDateType ->
                            if (name.isBlank()) {
                                Toast.makeText(context, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.saveIndividual(
                                    name = name,
                                    alias = alias,
                                    gender = gender,
                                    relativeId = relativeId,
                                    relation = relation,
                                    birthDateCode = birthDateCode,
                                    birthDateType = birthDateType
                                ) {
                                    Toast.makeText(context, "저장되었습니다.", Toast.LENGTH_SHORT).show()
                                    onSuccess()
                                }
                            }
                        }
                    )
                }
                is EditIndividualUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("오류: ${(state as EditIndividualUiState.Error).throwable.localizedMessage}")
                    }
                }
            }
        }
    }
}

@Composable
fun EditForm(
    data: EditIndividualUiState.Success,
    personId: Long,
    onItemClick: (NavKey) -> Unit,
    onSave: (String, String, Int, Long, Int, Long, Int) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(data.name) }
    var alias by remember { mutableStateOf(data.alias) }
    var gender by remember { mutableStateOf(data.gender) } // 1: 남성, 2: 여성

    // 관계 설정 필드
    var relativeId by remember { mutableStateOf(data.relativeId) }
    var relation by remember { mutableStateOf(data.relation) }

    // 생년월일 필드
    var birthYear by remember { mutableStateOf("") }
    var birthMonth by remember { mutableStateOf("") }
    var birthDay by remember { mutableStateOf("") }
    var birthDateType by remember { mutableStateOf(data.birthDateType) } // 0: 양력, 1: 음력

    // 기존 출생일 파싱
    LaunchedEffect(data.birthDateCode) {
        val dateVal = data.birthDateCode
        if (dateVal in 10000000..99991231) {
            birthYear = (dateVal / 10000).toString()
            birthMonth = ((dateVal % 10000) / 100).toString()
            birthDay = (dateVal % 100).toString()
        }
    }

    var showRelativeMenu by remember { mutableStateOf(false) }
    var showRelationMenu by remember { mutableStateOf(false) }

    val relationsMap = mapOf(
        0 to "지정하지 않음",
        1 to "부모",
        2 to "배우자",
        3 to "자녀",
        4 to "형제자매"
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("기본 정보", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("이름 *") },
            placeholder = { Text("이름을 입력하세요") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = alias,
            onValueChange = { alias = it },
            label = { Text("호칭 / 별명") },
            placeholder = { Text("호칭을 입력하세요 (예: 큰아버지, 삼촌)") },
            modifier = Modifier.fillMaxWidth()
        )

        Text("성별 선택 *", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Column(Modifier.selectableGroup()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .selectable(selected = (gender == 1), onClick = { gender = 1 }, role = Role.RadioButton),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = (gender == 1), onClick = null)
                Text(text = "남성", modifier = Modifier.padding(start = 12.dp))
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .selectable(selected = (gender == 2), onClick = { gender = 2 }, role = Role.RadioButton),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = (gender == 2), onClick = null)
                Text(text = "여성", modifier = Modifier.padding(start = 12.dp))
            }
        }

        HorizontalDivider()
        Text("관계도 설정", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        // 연관 인물 선택
        val selectedRelative = data.individuals.firstOrNull { it.id == relativeId }
        Text("연관 대상 인물", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Box {
            OutlinedButton(
                onClick = { showRelativeMenu = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selectedRelative?.name ?: "연관된 인물 선택 (선택 사항)")
            }
            DropdownMenu(
                expanded = showRelativeMenu,
                onDismissRequest = { showRelativeMenu = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                DropdownMenuItem(
                    text = { Text("지정하지 않음") },
                    onClick = {
                        relativeId = -1L
                        showRelativeMenu = false
                    }
                )
                data.individuals.forEach { person ->
                    val aliasText = if (!person.alias.isNullOrBlank()) " (${person.alias})" else ""
                    DropdownMenuItem(
                        text = { Text("${person.name}$aliasText") },
                        onClick = {
                            relativeId = person.id
                            showRelativeMenu = false
                        }
                    )
                }
            }
        }

        // 관계 유형 선택
        Text("나와의 관계", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Box {
            OutlinedButton(
                onClick = { showRelationMenu = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(relationsMap[relation] ?: "관계 선택")
            }
            DropdownMenu(
                expanded = showRelationMenu,
                onDismissRequest = { showRelationMenu = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                relationsMap.forEach { (relCode, relLabel) ->
                    DropdownMenuItem(
                        text = { Text(relLabel) },
                        onClick = {
                            relation = relCode
                            showRelationMenu = false
                        }
                    )
                }
            }
        }

        HorizontalDivider()
        Text("출생일 등록 (생일 기념일 자동 생성)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = (birthDateType == 0), onClick = { birthDateType = 0 })
                Text("양력", modifier = Modifier.padding(start = 4.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = (birthDateType == 1), onClick = { birthDateType = 1 })
                Text("음력", modifier = Modifier.padding(start = 4.dp))
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = birthYear,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }
                    if (filtered.length <= 4) birthYear = filtered
                },
                label = { Text("년") },
                placeholder = { Text("YYYY") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1.2f)
            )
            OutlinedTextField(
                value = birthMonth,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }
                    if (filtered.length <= 2) birthMonth = filtered
                },
                label = { Text("월") },
                placeholder = { Text("MM") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(0.9f)
            )
            OutlinedTextField(
                value = birthDay,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }
                    if (filtered.length <= 2) birthDay = filtered
                },
                label = { Text("일") },
                placeholder = { Text("DD") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(0.9f)
            )
        }

        // 등록된 다른 기념일 목록 및 [+] 추가 기능 구현 (수정 모드일 때만 노출)
        if (!data.isNew) {
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "등록된 기념일 목록", 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = { onItemClick(EditFact(-1L, "IND_$personId")) }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "기념일 추가")
                }
            }
            
            // 출생 팩트(type = 3)를 제외한 나머지 팩트 리스트 출력
            val otherFacts = data.facts.filter { it.type != 3 }
            if (otherFacts.isEmpty()) {
                Text(
                    text = "등록된 다른 기념일이 없습니다. 우측 [+] 버튼을 눌러 결혼, 회갑 등의 기념일을 추가해보세요.", 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                otherFacts.forEach { fact ->
                    val dateStr = formatLongDate(fact.startDate)
                    val calendarTypeStr = if (fact.dateType == 1) " (음력)" else " (양력)"
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemClick(EditFact(fact.id, "IND_$personId")) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = fact.label ?: "기념일", fontWeight = FontWeight.Bold)
                            Text(text = "날짜: $dateStr$calendarTypeStr", style = MaterialTheme.typography.bodyMedium)
                            if (!fact.place.isNullOrBlank()) {
                                Text(text = "장소: ${fact.place}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val y = birthYear.toIntOrNull() ?: 0
                val m = birthMonth.toIntOrNull() ?: 0
                val d = birthDay.toIntOrNull() ?: 0
                
                // 생년월일을 적기 시작했을 때 유효성 검증
                val hasAnyDateInput = birthYear.isNotBlank() || birthMonth.isNotBlank() || birthDay.isNotBlank()
                if (hasAnyDateInput) {
                    if (y !in 1000..9999 || m !in 1..12 || d !in 1..31) {
                        Toast.makeText(context, "유효한 생년월일(년: 1000~9999, 월: 1~12, 일: 1~31)을 입력해주세요.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                }

                val birthCode = if (y > 0 && m > 0 && d > 0) {
                    (y * 10000 + m * 100 + d).toLong()
                } else {
                    0L
                }

                onSave(name, alias, gender, relativeId, relation, birthCode, birthDateType)
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
