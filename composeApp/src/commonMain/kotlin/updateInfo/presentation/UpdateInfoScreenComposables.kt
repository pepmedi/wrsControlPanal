package updateInfo.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import controlPanalUser.presentation.PanelUserCreationScreenRoot
import doctor.presentation.AddDoctorScreen
import hospital.presentation.AddHospitalScreen
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import services.presentation.AddServicesScreenUI
import wrscontrolpanel.composeapp.generated.resources.Res
import wrscontrolpanel.composeapp.generated.resources.compose_multiplatform
import wrscontrolpanel.composeapp.generated.resources.medical_services

@Preview
@Composable
fun UpdateInfoScreen() {
    var addDoctorUiScreen by remember { mutableStateOf(false) }
    var addHospitalUiScreen by remember { mutableStateOf(false) }
    var addServicesUiScreen by remember { mutableStateOf(false) }
    MaterialTheme {
        Scaffold(
            containerColor = Color.White
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row {
                        ClickableCard(text = "Add Doctor", icon = Res.drawable.compose_multiplatform, onClick = {
                            addDoctorUiScreen = true
                        })
                        Spacer(modifier = Modifier.size(10.dp))

                        ClickableCard(
                            text = "Add Hospital",
                            icon = Res.drawable.compose_multiplatform,
                            onClick = {
                                addHospitalUiScreen = true
                            })

                        ClickableCard(
                            text = "Add Services",
                            icon = Res.drawable.medical_services,
                            onClick = {
                                addServicesUiScreen = true
                            })
                    }
                }
            }
            if (addDoctorUiScreen) {
                AddDoctorScreen(onBackClick = {
                    addDoctorUiScreen = false
                })
            }

            if (addHospitalUiScreen) {
                AddHospitalScreen(onBackClick = {
                    addHospitalUiScreen = false
                })
            }
            if (addServicesUiScreen) {
                AddServicesScreenUI(onBackClick = {
                    addServicesUiScreen = false
                })
            }
        }
    }
}

@Composable
fun ClickableCard(
    text: String,
    icon: DrawableResource,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .size(width = 120.dp, height = 100.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = text,
                tint = Color.Black,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.Black
            )
        }
    }
}