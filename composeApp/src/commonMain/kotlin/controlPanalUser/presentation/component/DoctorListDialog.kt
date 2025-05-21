package controlPanalUser.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState
import com.wearespine.`in`.theme.keylineDimen8
import doctor.domain.DoctorMaster
import theme.AppButton
import theme.ButtonType
import theme.ButtonViewState
import theme.Gap
import util.Util.toNameFormat

@Composable
fun DoctorListDialog(
    doctorList: List<DoctorMaster>,
    onDismiss: () -> Unit,
    onSubmit: (DoctorMaster) -> Unit
) {
    DialogWindow(
        onCloseRequest = onDismiss,
        title = "Select Doctor",
        state = rememberDialogState(size = DpSize(500.dp, 500.dp)),
        resizable = true
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
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Doctors",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 5.dp)
                )

                Spacer(modifier = Modifier.height(5.dp))



                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(8.dp)
                ) {
                    items(doctorList) { doctor ->
                        DoctorNameItem(
                            doctor = doctor,
                            onSelect = { selectedDoctor ->
                                onSubmit(selectedDoctor)
                                onDismiss()
                            })
                    }
                }
            }
        }
    }
}

@Composable
fun DoctorNameItem(
    doctor: DoctorMaster,
    onSelect: (DoctorMaster) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .clickable { onSelect(doctor) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.Black),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = doctor.name.toNameFormat(),
                style = TextStyle(fontSize = 16.sp),
                modifier = Modifier.padding(1.dp)
            )
        }
    }
}


@Composable
fun DoctorListMultiDialog(
    doctorList: List<DoctorMaster>,
    selectedDoctors: List<DoctorMaster>,
    onDismiss: () -> Unit,
    onSubmit: (List<DoctorMaster>) -> Unit
) {

    val selectedDoctorList = remember {
        mutableStateListOf<DoctorMaster>().apply {
            addAll(selectedDoctors)
        }
    }

    DialogWindow(
        onCloseRequest = onDismiss,
        title = "Select Doctors",
        state = rememberDialogState(size = DpSize(500.dp, 500.dp)),
        resizable = true
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
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Doctors",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 5.dp)
                )

                Spacer(modifier = Modifier.height(5.dp))


                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(8.dp)
                ) {
                    items(doctorList) { doctor ->
                        DoctorNameItem(
                            doctor = doctor,
                            onSelect = { selectedDoctor ->
                                if (selectedDoctor in selectedDoctorList) {
                                    selectedDoctorList.remove(selectedDoctor)
                                } else {
                                    selectedDoctorList.add(selectedDoctor)
                                }
                            })
                    }
                }

                AppButton(
                    text = "Submit",
                    onClick = {
                        onSubmit(selectedDoctorList)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    enabled = selectedDoctorList.isNotEmpty(),
                    buttonType = ButtonType.LARGE,
                )

                Gap(height = keylineDimen8)

                AppButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    buttonType = ButtonType.LARGE_OUTLINE,
                    viewState = ButtonViewState.ERROR
                )
            }
        }
    }
}