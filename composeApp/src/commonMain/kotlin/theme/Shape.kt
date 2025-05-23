package com.wearespine.`in`.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp
import com.wearespine.`in`.theme.AppShape.ExtraSmall
import com.wearespine.`in`.theme.AppShape.Small
import com.wearespine.`in`.theme.AppShape.Medium
import com.wearespine.`in`.theme.AppShape.Large
import com.wearespine.`in`.theme.AppShape.ExtraLarge

object AppShape {
    val ExtraSmall = RoundedCornerShape(4.dp)
    val Small = RoundedCornerShape(8.dp)
    val Medium = RoundedCornerShape(12.dp)
    val Large = RoundedCornerShape(16.dp)
    val ExtraLarge = RoundedCornerShape(24.dp)
    val Full = RoundedCornerShape(50)
}

val AppShapes = Shapes(
    extraSmall = ExtraSmall,
    small = Small,
    medium = Medium,
    large = Large,
    extraLarge = ExtraLarge
)