package kr.stonecold.myheritage.ui.note

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import kr.stonecold.myheritage.data.NoteEntity
import kotlinx.coroutines.launch

@Composable
fun EditNoteScreen(
    noteId: Long,
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
    var description by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf(0) }
    var isPrivate by remember { mutableStateOf(0) } // 0: true, 1: false
    var type by remember { mutableStateOf(0) }
    
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(noteId) {
        isLoading = true
        if (noteId != -1L) {
            val note = repository.getNoteById(noteId)
            if (note != null) {
                label = note.label ?: ""
                description = note.description
                kind = note.kind
                isPrivate = note.isPrivate
                type = note.type ?: 0
            }
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(if (noteId == -1L) "메모 작성" else "메모 수정") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
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
                        label = { Text("제목 / 구분") },
                        placeholder = { Text("메모의 주제를 입력하세요") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("메모 내용 *") },
                        placeholder = { Text("내용을 입력하세요") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        maxLines = 10
                    )

                    // 비공개 여부 선택
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = (isPrivate == 0),
                            onCheckedChange = { checked -> isPrivate = if (checked) 0 else 1 }
                        )
                        Text("비공개 메모 지정")
                    }

                    Button(
                        onClick = {
                            if (description.isBlank()) {
                                Toast.makeText(context, "메모 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            coroutineScope.launch {
                                val entity = NoteEntity(
                                    id = if (noteId == -1L) 0 else noteId,
                                    possessId = possessId,
                                    kind = kind,
                                    type = type,
                                    label = label.trim().ifEmpty { null },
                                    description = description.trim(),
                                    isPrivate = isPrivate,
                                    lastModified = System.currentTimeMillis()
                                )

                                if (noteId == -1L) {
                                    repository.insertNote(entity)
                                } else {
                                    repository.updateNote(entity)
                                }
                                Toast.makeText(context, "메모가 저장되었습니다.", Toast.LENGTH_SHORT).show()
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
