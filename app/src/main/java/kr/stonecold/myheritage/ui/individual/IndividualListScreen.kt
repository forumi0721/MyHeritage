package kr.stonecold.myheritage.ui.individual

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import kr.stonecold.myheritage.*

@Composable
fun IndividualListScreen(
    onItemClick: (NavKey) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as MyHeritageApplication
    val viewModel: IndividualListViewModel = viewModel {
        IndividualListViewModel(app.repository)
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("인물 목록") },
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
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onItemClick(EditIndividual(-1L)) }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "인물 추가")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 검색 필드
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                label = { Text("이름 또는 호칭 검색") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "검색") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            when (state) {
                IndividualListUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is IndividualListUiState.Success -> {
                    val list = (state as IndividualListUiState.Success).list
                    if (list.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("등록된 인물이 없습니다.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(list) { person ->
                                IndividualItem(
                                    person = person,
                                    onItemClick = { onItemClick(IndividualDetail(person.id)) },
                                    onEditClick = { onItemClick(EditIndividual(person.id)) },
                                    onTreeClick = { onItemClick(FamilyTree(person.id)) }
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                }
                is IndividualListUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("오류: ${(state as IndividualListUiState.Error).throwable.localizedMessage}")
                    }
                }
            }
        }
    }
}

@Composable
fun IndividualItem(
    person: kr.stonecold.myheritage.data.IndividualEntity,
    onItemClick: () -> Unit,
    onEditClick: () -> Unit,
    onTreeClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 성별 아이콘 표시
        val genderIcon = if (person.gender == 1) Icons.Default.Male else Icons.Default.Female
        val genderColor = if (person.gender == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        Icon(
            imageVector = genderIcon,
            contentDescription = if (person.gender == 1) "남성" else "여성",
            tint = genderColor,
            modifier = Modifier.size(28.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = person.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            if (!person.alias.isNullOrBlank()) {
                Text(
                    text = "호칭: ${person.alias}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 바로가기 버튼들
        IconButton(onClick = onTreeClick) {
            Icon(imageVector = Icons.Default.Share, contentDescription = "가계도", tint = MaterialTheme.colorScheme.secondary)
        }
        IconButton(onClick = onEditClick) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = "편집", tint = MaterialTheme.colorScheme.outline)
        }
    }
}
