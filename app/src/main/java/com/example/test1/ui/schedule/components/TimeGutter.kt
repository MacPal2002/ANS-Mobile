package com.example.test1.ui.schedule.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.test1.ui.schedule.DayStartHour

@SuppressLint("DefaultLocale")
@Composable
fun TimeGutter(hourHeight: Dp, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        for (hour in DayStartHour..22) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(hourHeight),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = String.format("%d:00", hour),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }
    }
}