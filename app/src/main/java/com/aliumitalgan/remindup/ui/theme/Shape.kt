package com.aliumitalgan.remindup.ui.theme

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = androidx.compose.material3.Shapes().small.copy(topStart = CornerSize(16.dp), topEnd = CornerSize(16.dp)),
    medium = androidx.compose.material3.Shapes().medium.copy(topStart = CornerSize(16.dp), topEnd = CornerSize(16.dp)),
    large = androidx.compose.material3.Shapes().large.copy(topStart = CornerSize(32.dp), topEnd = CornerSize(32.dp))
)