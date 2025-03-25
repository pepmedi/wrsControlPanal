package dashboard

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.Route
import doctor.presentation.AddDoctorScreen
import doctor.presentation.DoctorListScreenRoot
import hospital.presentation.AddHospitalScreen

@Preview
@Composable
fun DashboardScreenUi() {
    val scope = rememberCoroutineScope()
    var addDoctorUiScreen by remember { mutableStateOf(false) }
    // Main Layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 500.dp),
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(
                    elevation = CardDefaults.cardElevation(8.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(16.dp)
                        .align(Alignment.TopStart)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .widthIn(max = 600.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                    }
                }
            }

            item {
                Card(
                    elevation = CardDefaults.cardElevation(8.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(16.dp)
                        .align(Alignment.TopStart)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .widthIn(max = 600.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                    }
                }
            }

            item {
                Card(
                    elevation = CardDefaults.cardElevation(8.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(16.dp)
                        .align(Alignment.TopStart)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .widthIn(max = 600.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                    }
                }
            }

        }

        // Floating Action Button placed independently at the bottom right
        FloatingActionButton(
            onClick = {
                addDoctorUiScreen = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            shape = RoundedCornerShape(corner = CornerSize(8.dp))
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Shop")
        }

        if (addDoctorUiScreen) {
            AddDoctorScreen (onBackClick = {
                addDoctorUiScreen = false
            })
        }
    }
}





