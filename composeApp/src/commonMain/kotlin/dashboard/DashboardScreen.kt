package dashboard

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import core.FileSelector

@Preview
@Composable
fun DashboardScreenUi() {
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var fileName by remember { mutableStateOf("") }

//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White),
//        contentAlignment = Alignment.Center
//    ) {
//        Card(
//            elevation = CardDefaults.cardElevation(8.dp),
//            shape = RoundedCornerShape(16.dp),
//            modifier = Modifier.padding(16.dp)
//                .align(Alignment.TopStart)
//        ) {
//            FileSelector(
//                imageBitmap = imageBitmap,
//                fileName = fileName,
//                errorMessage = { },
//                onFileSelected = { _,_ -> },
//            )
//        }
//    }
}





