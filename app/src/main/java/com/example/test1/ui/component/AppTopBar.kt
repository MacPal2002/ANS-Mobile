package com.example.test1.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun AppTopBar(
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
    onNavigationIconClick: (() -> Unit)? = null,
    actionIcon: ImageVector? = null,
    onActionClick: (() -> Unit)? = null
) {
/*    val primaryColor = Color(0xFF212C5D)
    val secondaryColor = Color(0xFFE8EAF6)*/

    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top=38.dp),
            color = MaterialTheme.colorScheme.background,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(64.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Icon Slot
                Box(modifier = Modifier.size(40.dp)) {
                    if (navigationIcon != null && onNavigationIconClick != null) {
                        IconButton(
                            onClick = onNavigationIconClick,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(
                                imageVector = navigationIcon,
                                contentDescription = "Navigation Icon",
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    AcademyTitle()
                }

                // Right Icon Slot
                Box(modifier = Modifier.size(40.dp)) {
                    if (actionIcon != null && onActionClick != null) {
                        IconButton(
                            onClick = onActionClick,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(
                                imageVector = actionIcon,
                                contentDescription = "Action Icon",
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                }
            }
        }
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
}


@Preview("Final Top Bar")
@Composable
fun PreviewFinalTopBar() {
    AppTopBar(
        navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
        onNavigationIconClick = { },
        actionIcon = Icons.Default.Settings,
        onActionClick = { }
    )
}