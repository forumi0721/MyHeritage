package kr.stonecold.myheritage.ui.family

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
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
import kr.stonecold.myheritage.*
import kr.stonecold.myheritage.data.FamilyEntity
import kr.stonecold.myheritage.data.IndividualEntity
import kotlinx.coroutines.launch

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
    val coroutineScope = rememberCoroutineScope()

    var family by remember { mutableStateOf<FamilyEntity?>(null) }
    var husband by remember { mutableStateOf<IndividualEntity?>(null) }
    var wife by remember { mutableStateOf<IndividualEntity?>(null) }
    var children by remember { mutableStateOf<List<IndividualEntity>>(emptyList()) }
    var allIndividuals by remember { mutableStateOf<List<IndividualEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var showAddChildMenu by remember { mutableStateOf(false) }

    fun loadData() {
        coroutineScope.launch {
            val fam = repository.getFamilyById(familyId)
            if (fam != null) {
                family = fam
                husband = if (fam.husbandId != -1L) repository.getIndividualById(fam.husbandId) else null
                wife = if (fam.wifeId != -1L) repository.getIndividualById(fam.wifeId) else null
                children = repository.getIndividualsByFamilyIdChild(fam.id)
                allIndividuals = repository.getAllIndividualsDirect()
            }
        }
    }

    LaunchedEffect(familyId) {
        isLoading = true
        loadData()
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
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = "가족 정보", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    
                    // 부부 노드 카드
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "배우자 연결", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (husband != null) {
                                val hAlias = if (!husband!!.alias.isNullOrBlank()) " (${husband!!.alias})" else ""
                                Text(
                                    text = "남편: ${husband!!.name}$hAlias",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onItemClick(IndividualDetail(husband!!.id)) }
                                        .padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Text(text = "남편: 지정 안 됨", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (wife != null) {
                                val wAlias = if (!wife!!.alias.isNullOrBlank()) " (${wife!!.alias})" else ""
                                Text(
                                    text = "아내: ${wife!!.name}$wAlias",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onItemClick(IndividualDetail(wife!!.id)) }
                                        .padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Text(text = "아내: 지정 안 됨", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    HorizontalDivider()

                    // 자녀 세대 헤더 및 추가 버튼
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "자녀 세대 목록", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        
                        // 자녀 추가 드롭다운 트리거 버튼
                        Box {
                            IconButton(onClick = { showAddChildMenu = true }) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "자녀 추가")
                            }
                            
                            // 현재 가족의 자녀가 아닌 다른 인물들 필터링
                            val availableChildren = allIndividuals.filter { 
                                it.familyIdChild != family!!.id && it.id != husband?.id && it.id != wife?.id
                            }
                            
                            DropdownMenu(
                                expanded = showAddChildMenu,
                                onDismissRequest = { showAddChildMenu = false }
                            ) {
                                if (availableChildren.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("자녀로 추가할 인물이 없습니다.") },
                                        onClick = { showAddChildMenu = false }
                                    )
                                } else {
                                    availableChildren.forEach { childCand ->
                                        val candAlias = if (!childCand.alias.isNullOrBlank()) " (${childCand.alias})" else ""
                                        DropdownMenuItem(
                                            text = { Text("${childCand.name}$candAlias") },
                                            onClick = {
                                                coroutineScope.launch {
                                                    repository.updateIndividual(
                                                        childCand.copy(familyIdChild = family!!.id)
                                                    )
                                                    Toast.makeText(context, "${childCand.name} 자녀가 추가되었습니다.", Toast.LENGTH_SHORT).show()
                                                    loadData()
                                                }
                                                showAddChildMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (children.isEmpty()) {
                        Text(text = "등록된 자녀가 없습니다. 우측 상단의 [+] 버튼을 눌러 자녀 인물을 이 가족에 연결해 주세요.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        children.forEach { child ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onItemClick(IndividualDetail(child.id)) }
                                    ) {
                                        val childAlias = if (!child.alias.isNullOrBlank()) " (${child.alias})" else ""
                                        Text(
                                            text = "${child.name}$childAlias",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = "성별: ${if (child.gender == 1) "남성" else "여성"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    
                                    // 자녀 관계 해제 버튼
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                repository.updateIndividual(child.copy(familyIdChild = -1L))
                                                Toast.makeText(context, "${child.name} 자녀 연결을 해제했습니다.", Toast.LENGTH_SHORT).show()
                                                loadData()
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close, 
                                            contentDescription = "자녀 관계 끊기",
                                            tint = MaterialTheme.colorScheme.error
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
}
