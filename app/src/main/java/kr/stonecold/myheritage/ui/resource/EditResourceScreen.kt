package kr.stonecold.myheritage.ui.resource

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.stonecold.myheritage.*
import kr.stonecold.myheritage.data.ResourceEntity
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

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

    // 파일 선택 런처 구성
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { selectedUri ->
        if (selectedUri != null) {
            coroutineScope.launch {
                try {
                    val contentResolver = context.contentResolver
                    val ext = getFileExtension(context, selectedUri)
                    val resourcesDir = File(context.filesDir, "resources")
                    if (!resourcesDir.exists()) {
                        resourcesDir.mkdirs()
                    }
                    val targetFile = File(resourcesDir, "media_${System.currentTimeMillis()}.$ext")

                    contentResolver.openInputStream(selectedUri)?.use { input ->
                        FileOutputStream(targetFile).use { output ->
                            input.copyTo(output)
                        }
                    }

                    uri = targetFile.absolutePath

                    if (label.isBlank()) {
                        label = "미디어 파일"
                    }

                    Toast.makeText(context, "로컬 파일이 앱 내부 저장소로 복사되었습니다.", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "파일 가져오기 실패: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // 비트맵 미리보기 로드
    val bitmap = remember(uri) {
        if (uri.isNotBlank() && uri.startsWith("/")) {
            try {
                android.graphics.BitmapFactory.decodeFile(uri)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    val scrollState = rememberScrollState()

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
                        .verticalScroll(scrollState)
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
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("선택된 내부 파일 경로 *") },
                        placeholder = { Text("아래 버튼을 눌러 파일을 선택하세요") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { launcher.launch("*/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("로컬 미디어 파일 선택 (*/*)")
                    }

                    // 이미지 비트맵 미리보기 표시
                    if (bitmap != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(vertical = 8.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "선택된 미디어 미리보기",
                                    modifier = Modifier.fillMaxHeight()
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("상세 설명") },
                        placeholder = { Text("설명 기입") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (label.isBlank() || uri.isBlank()) {
                                Toast.makeText(context, "라벨을 입력하고 파일을 선택해주세요.", Toast.LENGTH_SHORT).show()
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

// 확장자 추출 헬퍼 함수
fun getFileExtension(context: android.content.Context, uri: android.net.Uri): String {
    val mimeType = context.contentResolver.getType(uri)
    return if (mimeType != null) {
        android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "bin"
    } else {
        val path = uri.path
        if (path != null) {
            val idx = path.lastIndexOf('.')
            if (idx != -1) path.substring(idx + 1) else "bin"
        } else {
            "bin"
        }
    }
}
