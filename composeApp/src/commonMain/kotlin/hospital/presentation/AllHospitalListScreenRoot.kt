package hospital.presentation

import PrimaryAppColor
import SecondaryAppColor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.ripple
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import component.AppCircularProgressIndicator
import component.SlideInScreen
import core.components.SearchBar
import hospital.domain.HospitalMaster
import hospital.presentation.components.UpdateHospitalScreen
import org.koin.compose.viewmodel.koinViewModel
import util.Util.toNameFormat
import util.toTitleCase

@Composable
fun AllHospitalListScreenRoot(viewmodel: HospitalViewModel = koinViewModel()) {
    val uiState by viewmodel.uiState.collectAsStateWithLifecycle()

    AllHospitalListScreen(
        uiState = uiState,
        onAction = { action ->
            viewmodel.onAction(action)
        }
    )
}

@Composable
fun AllHospitalListScreen(
    uiState: HospitalUiState,
    onAction: (HospitalActions) -> Unit
) {
    var expandedCardId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    var addHospitalUiScreen by remember { mutableStateOf(false) }

    var showUpdateHospitalScreen by remember { mutableStateOf(false) }
    var currentHospitalId by remember { mutableStateOf("") }

    val displayedHospitals = uiState.hospitalsList.filter {
        it.name.contains(searchQuery, ignoreCase = true)
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
                    columns = GridCells.Adaptive(minSize = 400.dp),
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = displayedHospitals,
                        key = { it.id }) { hospital ->
                        AnimatedVisibility(
                            visible = hospital in displayedHospitals,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            HospitalCard(
                                hospital = hospital,
                                modifier = Modifier.animateItem(),
                                onUpdateClick = {
                                    currentHospitalId = hospital.id
                                    showUpdateHospitalScreen = true
                                },
                                isExpanded = expandedCardId == hospital.id,
                                onExpand = { expandedCardId = hospital.id },
                                onCollapse = { expandedCardId = null },
                                onDeleteClick = { onAction(HospitalActions.OnDeleteHospital(hospital)) }
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { addHospitalUiScreen = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            shape = RoundedCornerShape(corner = CornerSize(8.dp)),
            containerColor = PrimaryAppColor
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Shop")
        }

        SlideInScreen(visible = addHospitalUiScreen) {
            AddHospitalScreen {
                addHospitalUiScreen = false
            }
        }

        SlideInScreen(visible = showUpdateHospitalScreen) {
            UpdateHospitalScreen(
                hospitalMaster = uiState.hospitalsList.find { it.id == currentHospitalId }!!,
                onBackClick = {
                    showUpdateHospitalScreen = false
                }
            )
        }
    }
}


@Composable
fun HospitalCard(
    hospital: HospitalMaster,
    modifier: Modifier,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    onDeleteClick: () -> Unit,
    onUpdateClick: () -> Unit
) {

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp, vertical = 5.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.LightGray)
            ) { if (isExpanded) onCollapse() else onExpand() },

        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp).animateContentSize()) {

            Column {
                Text(
                    text = hospital.name.toTitleCase(),
                    color = SecondaryAppColor,
                    fontSize = 20.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = hospital.address.toTitleCase(),
                    color = Color.Black,
                    fontSize = 14.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                )

                Spacer(modifier = Modifier.height(6.dp))

            }

            if (isExpanded) {

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {

                    OutlinedButton(
                        onClick = { onUpdateClick() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                        border = BorderStroke(1.dp, SecondaryAppColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Update", fontSize = 15.sp)
                    }

                    OutlinedButton(
                        onClick = { onDeleteClick() },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White,
                            containerColor = Color.Red
                        ),
                        border = BorderStroke(1.dp, SecondaryAppColor),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = "Delete", fontSize = 15.sp)
                    }
                }
            }
        }
    }
}