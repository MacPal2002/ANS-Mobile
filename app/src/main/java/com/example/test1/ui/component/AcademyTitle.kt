package com.example.test1.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.test1.R

@Preview
@Composable
fun AcademyTitle(
    fontSize: TextUnit = 24.sp,
) {

    Text(
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(color = colorResource(R.color.primaryColor))) {
                // Add a space right here
                append("ANS ")
            }
            withStyle(style = SpanStyle(color = colorResource(R.color.secondaryColor))) {
                append("NT")
            }
        },
        style = MaterialTheme.typography.titleLarge.copy(fontSize = fontSize)
    )
}