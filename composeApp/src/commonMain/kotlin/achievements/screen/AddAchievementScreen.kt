package achievements.screen

import achievements.data.AchievementMaster
import achievements.viewmodel.AchievementsActions
import achievements.viewmodel.AchievementsViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dokar.sonner.TextToastAction
import com.dokar.sonner.ToastType
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import com.wearespine.`in`.theme.keylineDimen16
import com.wearespine.`in`.theme.keylineDimen8
import component.GradientButton
import core.CancelButton
import core.ImageSelector
import doctor.screen.components.TextInputField
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import theme.AppButton
import theme.ButtonType
import theme.ButtonViewState
import theme.Gap
import util.FileCompressor
import util.FileUtil.loadAndCompressImage
import util.ToastEvent
import util.getCurrentTimeStamp
import java.io.File

@Composable
fun AddAchievementScreen(
    viewModel: AchievementsViewModel = koinViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    var achievementName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    val toaster = rememberToasterState()
    var toasterEvent by remember { mutableStateOf<ToastEvent?>(null) }

    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var imageFile by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(uiState.achievementAddedSuccessfully) {
        if (uiState.achievementAddedSuccessfully) {
            viewModel.onAction(AchievementsActions.OnAchievementAddedSuccessfully)
            toaster.show(
                message = "Achievement Added Successfully",
                type = ToastType.Success,
                action = TextToastAction(
                    text = "Done",
                    onClick = {
                        toaster.dismissAll()
                    }
                )
            )
            onBackClick()
        }
    }


    LaunchedEffect(toasterEvent?.id) {
        toasterEvent?.let {
            toaster.show(
                message = it.message,
                type = ToastType.Error,
                action = TextToastAction(
                    text = "Done",
                    onClick = { toaster.dismissAll() }
                )
            )
        }
    }

    fun validateForm(): Boolean {
        val errors = mutableListOf<String>()

        if (achievementName.isBlank()) errors.add("Achievement Name is required")
        if (description.isBlank()) errors.add("Description is required")
        if (imageFile == null) errors.add("Image is required")

        if (errors.isNotEmpty()) {
            scope.launch {
                toasterEvent = ToastEvent(errors.first())
            }
            return false
        }
        return true
    }


    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .background(Color.White)
                    .padding(10.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextInputField(
                    value = achievementName,
                    onValueChange = { achievementName = it },
                    label = "Achievement Name",
                    icon = Icons.Outlined.Home
                )


                TextInputField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description Address",
                    icon = Icons.Outlined.LocationOn
                )


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    //Service image
                    ImageSelector(
                        imageBitmap = imageBitmap,
                        onImageSelected = { file ->
                            scope.launch {
                                imageFile = FileCompressor.loadAndCompressImage(file)
                                imageBitmap = loadAndCompressImage(file)
                            }
                        },
                        errorMessage = { message ->
                            toasterEvent = ToastEvent(message)
                        },
                        text = "Select Achievement Image"
                    )
                }

                Gap(height = keylineDimen16)

                AppButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (validateForm() && imageFile != null) {
                            viewModel.onAction(
                                AchievementsActions.OnAddAchievement(
                                    achievementMaster = AchievementMaster(
                                        id = "",
                                        name = achievementName,
                                        description = description,
                                        createdAt = getCurrentTimeStamp(),
                                        updatedAt = getCurrentTimeStamp(),
                                        imageUrl = ""
                                    ),
                                    imageFile!!
                                )
                            )
                        }
                    },
                    text = "Add Achievement",
                    buttonType = ButtonType.LARGE,
                    viewState = if (uiState.isLoading) ButtonViewState.LOADING else ButtonViewState.DEFAULT
                )

                Gap(height = keylineDimen8)

                AppButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onBackClick()
                    },
                    text = "Back",
                    buttonType = ButtonType.LARGE_OUTLINE,
                )
            }

            Toaster(
                state = toaster,
                richColors = true,
                alignment = Alignment.TopEnd
            )
        }
    }
}