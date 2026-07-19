package kr.stonecold.myheritage.ui.settings

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
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
import kr.stonecold.myheritage.MyHeritageApplication

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as MyHeritageApplication
    val viewModel: SettingsViewModel = viewModel {
        SettingsViewModel(app.repository)
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var showBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    // SAF 파일 저장/오픈 런처 등록
    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
        onResult = { uri ->
            if (uri != null) {
                viewModel.backupDataToUri(uri)
            }
        }
    )

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.restoreDataFromUri(uri)
            }
        }
    )

    // 작업 결과 처리
    LaunchedEffect(state) {
        when (state) {
            is SettingsUiState.Success -> {
                val msg = (state as SettingsUiState.Success).message
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                viewModel.resetState()
                
                // 복원 성공인 경우, 새로운 DB 데이터 반영을 위해 액티비티 즉각 재기동(Recreate) 리프레시 실행!
                if (msg.contains("복원")) {
                    context.findActivity()?.recreate()
                }
            }
            is SettingsUiState.Error -> {
                Toast.makeText(context, "에러: ${(state as SettingsUiState.Error).throwable.localizedMessage}", Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("설정 및 데이터 관리") },
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
            if (state == SettingsUiState.Loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "화면 테마 설정",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "화면 모드 선택", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "앱의 화면 테마(라이트/다크/시스템 기본값)를 지정합니다.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            val prefs = remember { context.getSharedPreferences("my_heritage_prefs", Context.MODE_PRIVATE) }
                            var selectedTheme by remember { mutableStateOf(prefs.getInt("pref_theme_mode", 0)) }
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = (selectedTheme == 0),
                                        onClick = {
                                            prefs.edit().putInt("pref_theme_mode", 0).apply()
                                            selectedTheme = 0
                                        }
                                    )
                                    Text("시스템", modifier = Modifier.padding(start = 4.dp), fontSize = 13.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = (selectedTheme == 1),
                                        onClick = {
                                            prefs.edit().putInt("pref_theme_mode", 1).apply()
                                            selectedTheme = 1
                                        }
                                    )
                                    Text("라이트", modifier = Modifier.padding(start = 4.dp), fontSize = 13.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = (selectedTheme == 2),
                                        onClick = {
                                            prefs.edit().putInt("pref_theme_mode", 2).apply()
                                            selectedTheme = 2
                                        }
                                    )
                                    Text("다크", modifier = Modifier.padding(start = 4.dp), fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "데이터 백업 / 복원",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // 1. 백업 버튼
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "데이터 백업", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "가계 데이터를 안전하게 지정하신 기기 내 폴더와 파일명으로 저장합니다.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { showBackupDialog = true }) {
                                Text("데이터 백업 수행")
                            }
                        }
                    }

                    // 2. 복원 버튼
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "데이터 복원", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "기기에 백업된 파일(.db)을 직접 골라서 복원합니다. 복원 시 현재 입력 중인 데이터는 지워집니다.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { showRestoreDialog = true }) {
                                Text("데이터 복원 수행")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "위험 구역",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )

                    // 3. 데이터 초기화 버튼
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "모든 데이터 지우기", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "데이터베이스 내 모든 인물, 가족, 생애 이벤트 정보를 영구적으로 지우고 초기 상태로 되돌립니다. 복구가 불가능합니다.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showClearDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("데이터 전체 삭제")
                            }
                        }
                    }
                }
            }
        }
    }

    // 백업 다이얼로그
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text("데이터 백업 확인") },
            text = { Text("가계 정보를 지정할 폴더와 파일명으로 내보내시겠습니까?") },
            confirmButton = {
                TextButton(onClick = {
                    showBackupDialog = false
                    backupLauncher.launch("myHeritage_backup.db")
                }) {
                    Text("예")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    // 복원 다이얼로그
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("데이터 복원 경고") },
            text = { Text("백업 정보를 복원하게 되면 현재 입력된 데이터는 삭제되고 이전 백업 상태로 덮어씌워집니다. 복원할 백업 파일을 고르시겠습니까?") },
            confirmButton = {
                TextButton(onClick = {
                    showRestoreDialog = false
                    restoreLauncher.launch(arrayOf("*/*"))
                }) {
                    Text("복원 진행")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    // 초기화 다이얼로그
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = "경고", tint = MaterialTheme.colorScheme.error) },
            title = { Text("전체 데이터 삭제 확인") },
            text = { Text("이 작업은 되돌릴 수 없습니다. 정말 데이터베이스의 모든 정보를 영구히 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = {
                    showClearDialog = false
                    viewModel.clearData()
                }) {
                    Text("삭제 진행", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}

// Context에서 ComponentActivity를 안전하게 꺼내기 위한 헬퍼 확장 함수
fun android.content.Context.findActivity(): androidx.activity.ComponentActivity? {
    var currentContext = this
    while (currentContext is android.content.ContextWrapper) {
        if (currentContext is androidx.activity.ComponentActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}
