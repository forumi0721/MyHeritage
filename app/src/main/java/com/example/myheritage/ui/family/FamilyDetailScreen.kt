package com.example.myheritage.ui.family

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.example.myheritage.*
import com.example.myheritage.data.FamilyEntity
import com.example.myheritage.data.IndividualEntity

@Composable
fun FamilyDetailScreen(
    familyId: Long,
    onItemClick: (NavKey) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as MyHeritageApplication
    val repository = app.repository

    var family by remember { mutableStateOf<FamilyEntity?>(null) }
    var husband by remember { mutableStateOf<IndividualEntity?>(null) }
    var wife by remember { mutableStateOf<IndividualEntity?>(null) }
    var children by remember { mutableStateOf<List<IndividualEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(familyId) {
        isLoading = true
        val fam = repository.getFamilyById(familyId)
        if (fam != null) {
            family = fam
            if (fam.husbandId != -1L) husband = repository.getIndividualById(fam.husbandId)
            if (fam.wifeId != -1L) wife = repository.getIndividualById(fam.wifeId)
            children = repository.getIndividualsByFamilyIdChild(fam.id)
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("가족 상세 정보") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    IconButton(onClick = { onItemClick(EditFamily(familyId)) }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "가족 수정")
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
            } else if (family == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("가족 정보를 찾을 수 없습니다.")
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = "가족 요약", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    // 부부 노드
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "부부", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (husband != null) {
                                Text(
                                    text = "남편: ${husband!!.name} (호칭: ${husband!!.alias ?: "없음"})",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onItemClick(IndividualDetail(husband!!.id)) }
                                        .padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Text(text = "남편: 등록 정보 없음", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (wife != null) {
                                Text(
                                    text = "아내: ${wife!!.name} (호칭: ${wife!!.alias ?: "없음"})",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onItemClick(IndividualDetail(wife!!.id)) }
                                        .padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Text(text = "아내: 등록 정보 없음", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // 자녀 목록
                    Text(text = "자녀 세대", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (children.isEmpty()) {
                        Text(text = "등록된 자녀가 없습니다.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        children.forEach { child ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onItemClick(IndividualDetail(child.id)) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = child.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = if (child.gender == 1) "아들" else "딸",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
