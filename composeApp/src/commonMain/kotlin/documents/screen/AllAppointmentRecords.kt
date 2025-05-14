package documents.screen

import PrimaryAppColor
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import component.SlideInBottomSheet
import component.SlideInScreen
import core.components.SearchBar
import documents.modal.PatientMedicalRecordsMaster
import documents.viewModal.AllRecordsUiState
import documents.viewModal.AllRecordsViewModal
import org.koin.compose.viewmodel.koinViewModel
import util.AsyncImageViewer
import util.PdfViewerWithLoading
import util.Util.toNameFormat

@Composable
fun AllAppointmentRecordsRoot(
    appointmentId: String,
    viewModal: AllRecordsViewModal = koinViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModal.state.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }

    val filteredRecords = remember(searchQuery, uiState.records) {
        uiState.records.filter {
            it.appointmentId == appointmentId &&
                    it.recordName.contains(searchQuery, ignoreCase = true)
        }
    }

    AllAppointmentRecords(
        appointmentId = appointmentId,
        uiState = uiState,
        filteredRecords = filteredRecords,
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        onBackClick = {
            onBackClick()
        }
    )

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AllAppointmentRecords(
    appointmentId: String,
    uiState: AllRecordsUiState,
    filteredRecords: List<PatientMedicalRecordsMaster>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var expandedCardId by remember { mutableStateOf<String?>(null) }
    var showAddRecords by remember { mutableStateOf(false) }
    var showRecords by remember { mutableStateOf(false) }
    var currentClicked by remember { mutableStateOf(PatientMedicalRecordsMaster()) }

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                if (uiState.isLoading) {
                    AppCircularProgressIndicator()
                } else {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            SearchBar(
                                searchQuery = searchQuery,
                                onSearchQueryChange = { onSearchQueryChange(it) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                            )
                        }

                        // 🔽 Grid of records
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 400.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredRecords,
                                key = { it.id }) { record ->
                                MedicalRecordsCard(
                                    medicalRecord = record,
                                    modifier = Modifier.animateItem(
                                        fadeInSpec = null,
                                        fadeOutSpec = null
                                    ),
                                    isExpanded = expandedCardId == record.id,
                                    onExpand = { expandedCardId = record.id },
                                    onCollapse = { expandedCardId = null },
                                    onShowClick = {
                                        currentClicked = record
                                        showRecords = true
                                    }
                                )
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { showAddRecords = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                shape = RoundedCornerShape(corner = CornerSize(8.dp)),
                containerColor = PrimaryAppColor
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Shop")
            }

            SlideInBottomSheet(showAddRecords) {
                UploadAppointmentRecords(
                    appointmentId = appointmentId,
                    onSuccessfulUpload = {
                        showAddRecords = false
                    },
                    onBackClick = {
                        showAddRecords = false
                    })
            }

            SlideInScreen(showRecords) {
                if (currentClicked.mimeType.substringAfterLast("/") == "pdf")
                    PdfViewerWithLoading(currentClicked.fileUrl, onCancelClick = {
                        showRecords = false
                    })
                else
                    AsyncImageViewer(currentClicked.fileUrl,
                        onCancelClick = { showRecords = false })
            }
        }
    }
}

@Composable
fun MedicalRecordsCard(
    medicalRecord: PatientMedicalRecordsMaster,
    modifier: Modifier,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    onShowClick: (PatientMedicalRecordsMaster) -> Unit
) {
    Card(
        modifier = modifier
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
            RecordPreviewBox(medicalRecord.fileUrl)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = medicalRecord.recordName.toNameFormat(),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Text(
                text = medicalRecord.description.toNameFormat(),
                fontSize = 14.sp,
                color = Color.Gray
            )

            Text(
                text = "File Type:- ${
                    medicalRecord.mimeType.substringAfterLast("/").toNameFormat()
                }",
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
                        onClick = { onShowClick(medicalRecord) },
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text("Show")
                    }

                    TextButton(
                        modifier = Modifier.padding(end = 10.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.textButtonColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, PrimaryAppColor),
                        onClick = { }
                    ) {
                        Text("Update")
                    }
                }
            }
        }
    }
}

@Composable
fun RecordPreviewBox(url: String) {
    var loadFailed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        if (loadFailed) {
            // Fallback PDF icon if image fails to load
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "PDF File",
                tint = Color.Red,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            )
        } else {
            AsyncImage(
                model = url,
                contentDescription = "Medical Record Preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onError = { loadFailed = true }
            )
        }
    }
}

