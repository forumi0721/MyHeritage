package com.example.myheritage.ui.individual

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myheritage.MyHeritageApplication

@Composable
fun EditIndividualScreen(
    personId: Long,
    onSuccess: () -> Unit,
    onBackClick: () -> Unit,
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
                    EditForm(
                        data = state as EditIndividualUiState.Success,
                        onSave = { name, alias, gender ->
                            if (name.isBlank()) {
                                Toast.makeText(context, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.saveIndividual(name, alias, gender) {
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
    onSave: (String, String, Int) -> Unit
) {
    var name by remember { mutableStateOf(data.name) }
    var alias by remember { mutableStateOf(data.alias) }
    var gender by remember { mutableStateOf(data.gender) } // 1: 남성, 2: 여성

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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

        Text("성별 선택 *", style = MaterialTheme.typography.titleMedium)
        
        Column(Modifier.selectableGroup()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .selectable(
                        selected = (gender == 1),
                        onClick = { gender = 1 },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (gender == 1),
                    onClick = null // Row 클릭에서 처리
                )
                Text(
                    text = "남성",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .selectable(
                        selected = (gender == 2),
                        onClick = { gender = 2 },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (gender == 2),
                    onClick = null // Row 클릭에서 처리
                )
                Text(
                    text = "여성",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onSave(name, alias, gender) },
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
