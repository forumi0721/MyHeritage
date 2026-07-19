package kr.stonecold.myheritage.ui.resource

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
import kr.stonecold.myheritage.data.ResourceEntity
import kotlinx.coroutines.launch

@Composable
fun EditResourceScreen(
    resourceId: Long,
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
    var uri by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var rType by remember { mutableStateOf(0) } // 0: 이미지, 1: 오디오, 2: 비디오 등
    
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(resourceId) {
        isLoading = true
        if (resourceId != -1L) {
            val resource = repository.getResourceById(resourceId)
            if (resource != null) {
                label = resource.label ?: ""
                uri = resource.uri
                description = resource.description ?: ""
                rType = resource.rType
            }
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(if (resourceId == -1L) "미디어 리소스 등록" else "미디어 수정") },
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
                        label = { Text("미디어 제목 / 라벨 *") },
                        placeholder = { Text("예: 젊은 시절 사진, 졸업장 등") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uri,
                        onValueChange = { uri = it },
                        label = { Text("파일 경로 또는 URI *") },
                        placeholder = { Text("내부 저장소 이미지 경로 또는 링크 입력") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("상세 설명") },
                        placeholder = { Text("설명 기입") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            if (label.isBlank() || uri.isBlank()) {
                                Toast.makeText(context, "라벨과 파일 경로를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            coroutineScope.launch {
                                val entity = ResourceEntity(
                                    id = if (resourceId == -1L) 0 else resourceId,
                                    possessId = possessId,
                                    rType = rType,
                                    label = label.trim(),
                                    uri = uri.trim(),
                                    description = description.trim().ifEmpty { null },
                                    lastModified = System.currentTimeMillis()
                                )

                                if (resourceId == -1L) {
                                    repository.insertResource(entity)
                                } else {
                                    repository.updateResource(entity)
                                }
                                Toast.makeText(context, "미디어 리소스가 저장되었습니다.", Toast.LENGTH_SHORT).show()
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
