package doctor.screen

import BackgroundColors
import PrimaryAppColor
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import component.AppCircularProgressIndicator
import component.SlideInScreen
import core.components.SearchBar
import doctor.domain.DoctorMaster
import doctor.screen.components.UpdateDoctorDetailsScreenRoot
import doctor.viewModal.DoctorListActions
import doctor.viewModal.DoctorListUiState
import doctor.viewModal.DoctorListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.viewmodel.koinViewModel
import util.ToastEvent

@Composable
fun DoctorListScreenRoot(
    viewModal: DoctorListViewModel = koinViewModel(),
    onDoctorClick: (DoctorMaster) -> Unit
) {
    val uiState by viewModal.state.collectAsStateWithLifecycle()

    DoctorListScreen(uiState, onDoctorClick = {
        onDoctorClick(it)

    },
        onAction = { action ->
            viewModal.onAction(action)
        })
}

@Composable
fun DoctorListScreen(
    uiState: DoctorListUiState,
    onDoctorClick: (DoctorMaster) -> Unit,
    toasterEvent: (ToastEvent) -> Unit = {},
    onAction: (DoctorListActions) -> Unit
) {
    var expandedCardId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    var addDoctorUiScreen by remember { mutableStateOf(false) }
    var filteredDoctor by remember { mutableStateOf<List<DoctorMaster>>(emptyList()) }

    var showUpdateDoctorScreen by remember { mutableStateOf(false) }
    var currentDoctorId by remember { mutableStateOf("") }

    LaunchedEffect(searchQuery, uiState) {
        withContext(Dispatchers.Default) {
            val filtered = uiState.doctorList.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }
            withContext(Dispatchers.Main) {
                filteredDoctor = filtered
            }
        }
    }

    val displayedDoctors = remember(searchQuery, uiState.doctorList) {
        if (searchQuery.isNotEmpty()) filteredDoctor else uiState.doctorList
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

            if (uiState.isLoading) {
                AppCircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {

                SearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 300.dp),
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayedDoctors) { doctor ->
                        DoctorItem(
                            doctor, onDoctorClick,
                            isExpanded = expandedCardId == doctor.id,
                            onExpand = { expandedCardId = doctor.id },
                            onCollapse = { expandedCardId = null },
                            onUpdateClick = {
                                currentDoctorId = it
                                showUpdateDoctorScreen = true
                            }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { addDoctorUiScreen = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            shape = RoundedCornerShape(corner = CornerSize(8.dp))
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Shop")
        }

        SlideInScreen(visible = addDoctorUiScreen) {
            AddDoctorScreen(onBackClick = {
                addDoctorUiScreen = false
            })
        }

        SlideInScreen(visible = showUpdateDoctorScreen) {
            UpdateDoctorDetailsScreenRoot(doctorId = currentDoctorId, onBackClick = {
                showUpdateDoctorScreen = false
            },
                onSuccessful = {

                    toasterEvent(ToastEvent("Doctor Updated Successfully"))
                })
        }
    }
}

@Composable
fun DoctorItem(
    doctor: DoctorMaster,
    onDoctorClick: (DoctorMaster) -> Unit,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    onUpdateClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .then(
                if (isExpanded) Modifier.clickable(onClick = { onCollapse() }) else Modifier.clickable(
                    onClick = { onExpand() })
            ),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .animateContentSize()
        ) {
            val backgroundColor =
                remember { BackgroundColors.random() }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = doctor.profilePic,
                    contentDescription = "Doctor Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Inside
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = doctor.name,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Text(
                text = "Experience : ${doctor.experience} years",
                fontSize = 14.sp,
                color = Color.Gray
            )

            if (isExpanded) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        modifier = Modifier.padding(start = 0.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.textButtonColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, PrimaryAppColor),
                        onClick = { onDoctorClick(doctor) },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Info")
                    }

                    TextButton(
                        modifier = Modifier.padding(end = 10.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.textButtonColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, PrimaryAppColor),
                        onClick = { onUpdateClick(doctor.id) }
                    ) {
                        Text("Update")
                    }
                }
            }
        }
    }
}