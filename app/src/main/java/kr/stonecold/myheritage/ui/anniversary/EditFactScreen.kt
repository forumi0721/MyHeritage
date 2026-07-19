package kr.stonecold.myheritage.ui.anniversary

import android.widget.Toast
import androidx.compose.foundation.layout.*
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
import kr.stonecold.myheritage.*
import kr.stonecold.myheritage.data.FactEntity
import kotlinx.coroutines.launch

@Composable
fun EditFactScreen(
    factId: Long,
    possessId: String,
    onSuccess: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as MyHeritageApplication
    val repository = app.repository
    val coroutineScope = rememberCoroutineScope()

    var label by remember { mutableStateOf("") }
    var yearStr by remember { mutableStateOf("") }
    var monthStr by remember { mutableStateOf("") }
    var dayStr by remember { mutableStateOf("") }
    var place by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isRepeated by remember { mutableStateOf(1) } // 0: 매년반복, 1: 안함
    var type by remember { mutableStateOf(0) }
    var dateType by remember { mutableStateOf(0) } // 0: 양력, 1: 음력
    
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(factId) {
        isLoading = true
        if (factId != -1L) {
            val fact = repository.getFactById(factId)
            if (fact != null) {
                label = fact.label ?: ""
                place = fact.place ?: ""
                description = fact.description ?: ""
                isRepeated = fact.isRepeated
                type = fact.type
                dateType = fact.dateType
                
                // 날짜 파싱 (YYYYMMDD)
                val dateVal = fact.startDate
                if (dateVal in 10000000..99991231) {
                    yearStr = (dateVal / 10000).toString()
                    monthStr = ((dateVal % 10000) / 100).toString()
                    dayStr = (dateVal % 100).toString()
                } else if (dateVal > 0) {
                    val cal = java.util.Calendar.getInstance()
                    cal.setTimeInMillis(dateVal)
                    yearStr = cal.get(java.util.Calendar.YEAR).toString()
                    monthStr = (cal.get(java.util.Calendar.MONTH) + 1).toString()
                    dayStr = cal.get(java.util.Calendar.DAY_OF_MONTH).toString()
                }
            }
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(if (factId == -1L) "생애 이벤트 등록" else "이벤트 정보 수정") },
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("이벤트 구분 *") },
                        placeholder = { Text("예: 출생, 사망, 결혼, 회갑 등") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("날짜 입력 (양력/음력 선택) *", fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = (dateType == 0), onClick = { dateType = 0 })
                            Text("양력", modifier = Modifier.padding(start = 4.dp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = (dateType == 1), onClick = { dateType = 1 })
                            Text("음력", modifier = Modifier.padding(start = 4.dp))
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = yearStr,
                            onValueChange = { yearStr = it },
                            label = { Text("년") },
                            placeholder = { Text("YYYY") },
                            modifier = Modifier.weight(1.2f)
                        )
                        OutlinedTextField(
                            value = monthStr,
                            onValueChange = { monthStr = it },
                            label = { Text("월") },
                            placeholder = { Text("MM") },
                            modifier = Modifier.weight(0.9f)
                        )
                        OutlinedTextField(
                            value = dayStr,
                            onValueChange = { dayStr = it },
                            label = { Text("일") },
                            placeholder = { Text("DD") },
                            modifier = Modifier.weight(0.9f)
                        )
                    }

                    OutlinedTextField(
                        value = place,
                        onValueChange = { place = it },
                        label = { Text("장소") },
                        placeholder = { Text("장소를 입력하세요") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("설명 / 비고") },
                        placeholder = { Text("상세 정보 기술") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    // 매년 반복 여부 선택
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = (isRepeated == 0),
                            onCheckedChange = { checked -> isRepeated = if (checked) 0 else 1 }
                        )
                        Text("기념일 지정 (매년 기일/생일 알림에 반영)")
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            val y = yearStr.toIntOrNull() ?: 0
                            val m = monthStr.toIntOrNull() ?: 0
                            val d = dayStr.toIntOrNull() ?: 0
                            
                            if (label.isBlank()) {
                                Toast.makeText(context, "구분 라벨을 입력해주세요.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            val dateCode = (y * 10000 + m * 100 + d).toLong()
                            if (dateCode <= 0) {
                                Toast.makeText(context, "유효한 날짜를 입력해주세요.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            coroutineScope.launch {
                                val entity = FactEntity(
                                    id = if (factId == -1L) 0 else factId,
                                    possessId = possessId,
                                    type = type,
                                    label = label.trim(),
                                    startDate = dateCode,
                                    dateType = dateType,
                                    isRepeated = isRepeated,
                                    place = place.trim().ifEmpty { null },
                                    description = description.trim().ifEmpty { null },
                                    lastModified = System.currentTimeMillis()
                                )

                                if (factId == -1L) {
                                    repository.insertFact(entity)
                                } else {
                                    repository.updateFact(entity)
                                }
                                Toast.makeText(context, "생애 이벤트가 저장되었습니다.", Toast.LENGTH_SHORT).show()
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
