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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun DashboardScreenUi() {
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
    }
}





