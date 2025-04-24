package hospital.presentation.components

import SecondaryAppColor
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState
import core.CancelButton
import core.domain.DataError
import core.domain.AppResult
import hospital.domain.HospitalMaster

@Composable
fun HospitalListDialog(
    hospitalResult: AppResult<List<HospitalMaster>, DataError.Remote>? = null,
    hospitalList: List<HospitalMaster>? = null,
    selectedHospitalList: List<HospitalMaster> = emptyList(),
    onDismiss: () -> Unit,
    onSubmit: (List<HospitalMaster>) -> Unit
) {

    val resolvedResult = remember(hospitalResult, hospitalList) {
        when {
            hospitalResult != null -> hospitalResult
            hospitalList != null -> AppResult.Success(hospitalList)
            else -> AppResult.Error(DataError.Remote.UNKNOWN)
        }
    }

    val selectedHospitals = remember {
        mutableStateListOf<HospitalMaster>().apply {
            addAll(selectedHospitalList)
        }
    }

    DialogWindow(
        onCloseRequest = onDismiss,
        title = "Select Hospital",
        state = rememberDialogState(size = DpSize(500.dp, 500.dp)), // Window size
        resizable = false
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth()
                .height(800.dp)
                .wrapContentHeight(align = Alignment.Top),
            shape = RoundedCornerShape(0.dp),
            tonalElevation = 4.dp,
            color = Color.White
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Hospitals",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 5.dp)
                )

                Spacer(modifier = Modifier.height(5.dp))

                when (resolvedResult) {
                    is AppResult.Success -> {
                        val list = resolvedResult.data
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(8.dp)
                        ) {
                            items(list) { hospital ->
                                HospitalItem(
                                    hospital = hospital,
                                    isSelected = hospital in selectedHospitals,
                                    onSelect = { selectedHospital ->
                                        if (selectedHospital in selectedHospitals) {
                                            selectedHospitals.remove(selectedHospital)
                                        } else {
                                            selectedHospitals.add(selectedHospital)
                                        }
                                    })
                            }
                        }
                    }

                    is AppResult.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Failed to load hospitals: ${resolvedResult.error}",
                                color = Color.Red,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                Button(modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(5.dp),
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.White,
                        containerColor = SecondaryAppColor
                    ), onClick = {
                        onSubmit(selectedHospitals)
                        onDismiss()
                    }) {
                    Text("Submit")
                }
                CancelButton(onDismiss)
            }
        }
    }
}

@Composable
fun HospitalItem(
    hospital: HospitalMaster,
    isSelected: Boolean,
    onSelect: (HospitalMaster) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .clickable { onSelect(hospital) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (isSelected) Color.Green else Color.Black),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = hospital.name,
                style = TextStyle(fontSize = 16.sp),
                modifier = Modifier.padding(1.dp)
            )

            // Show checkmark if selected
            if (isSelected) {
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Selected",
                    tint = Color.Green
                )
            }
        }
    }
}
