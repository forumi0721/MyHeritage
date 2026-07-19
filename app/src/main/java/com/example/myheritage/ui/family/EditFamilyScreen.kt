package com.example.myheritage.ui.family

import android.widget.Toast
import androidx.compose.foundation.clickable
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
                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 남편 선택
                    val selectedHusband = individuals.firstOrNull { it.id == husbandId }
                    Text("남편 선택", fontWeight = FontWeight.Bold)
                    Box {
                        OutlinedButton(
                            onClick = { showHusbandMenu = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(selectedHusband?.name ?: "남편 인물 선택")
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
                                    text = { Text(person.name) },
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
                    Text("아내 선택", fontWeight = FontWeight.Bold)
                    Box {
                        OutlinedButton(
                            onClick = { showWifeMenu = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(selectedWife?.name ?: "아내 인물 선택")
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
                                    text = { Text(person.name) },
                                    onClick = {
                                        wifeId = person.id
                                        showWifeMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val entity = FamilyEntity(
                                    id = if (familyId == -1L) 0 else familyId,
                                    husbandId = husbandId,
                                    wifeId = wifeId,
                                    fStatus = fStatus,
                                    fKind = fKind,
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
