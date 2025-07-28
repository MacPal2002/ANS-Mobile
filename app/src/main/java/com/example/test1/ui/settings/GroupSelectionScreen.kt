package com.example.test1.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.test1.data.GroupNode
import com.example.test1.ui.component.AppTopBar

@Composable
fun GroupSelectionScreen(
    onNavigateBack: () -> Unit,
    viewModel: GroupSelectionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationIconClick = onNavigateBack,
                actionIcon = Icons.Default.Check, // Ikona "Zapisz"
                onActionClick = {
                    viewModel.onSaveSelection()
                    onNavigateBack() // Wróć do ustawień po zapisaniu
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = destructiveColor,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(uiState.allGroupsTree) { node ->
                        // Wywołujemy rekurencyjny komponent dla każdego elementu z drzewa
                        GroupNodeItem(
                            node = node,
                            selectedIds = uiState.selectedGroupIds,
                            onToggle = viewModel::onGroupToggled
                        )
                    }
                }
            }
        }
    }
}
/**
 * Rekurencyjnie sprawdza, czy dany węzeł lub którekolwiek z jego dzieci
 * zawiera grupę, która jest w zbiorze zaznaczonych ID.
 */
private fun containsSelection(node: GroupNode, selectedIds: Set<Int>): Boolean {
    // Przypadek bazowy: jeśli to jest grupa, sprawdź, czy jej ID jest w zbiorze
    if (node.type == "group" && node.groupId != null) {
        return selectedIds.contains(node.groupId)
    }
    // Krok rekurencyjny: sprawdź, czy KTÓREKOLWIEK z dzieci spełnia ten warunek
    return node.children.any { child -> containsSelection(child, selectedIds) }
}

/**
 * Rekurencyjny komponent do wyświetlania drzewa grup.
 */
@Composable
private fun GroupNodeItem(
    node: GroupNode,
    selectedIds: Set<Int>,
    onToggle: (Int, Boolean) -> Unit,
    level: Int = 0
) {
    val paddingStart = (level * 24).dp + 16.dp

    if (node.type == "group" && node.groupId != null) {
        // Ta część (dla liści z Checkboxem) pozostaje bez zmian
        val groupId = node.groupId
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle(groupId, !selectedIds.contains(groupId)) }
                .padding(start = paddingStart, end = 16.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = selectedIds.contains(groupId),
                onCheckedChange = { isChecked -> onToggle(groupId, isChecked) },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = node.name, style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        // ✅ ZMIANA: Logika dla rozwijanych węzłów (katalogów)

        // Używamy nowej funkcji, aby ustawić stan początkowy
        var isExpanded by remember { mutableStateOf(containsSelection(node, selectedIds)) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(start = paddingStart, end = 16.dp, top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = node.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = if (isExpanded) "Zwiń" else "Rozwiń",
                modifier = Modifier.rotate(if (isExpanded) 180f else 0f)
            )
        }

        if (isExpanded) {
            Column {
                node.children.forEach { child ->
                    GroupNodeItem(
                        node = child,
                        selectedIds = selectedIds,
                        onToggle = onToggle,
                        level = level + 1
                    )
                }
            }
        }
    }
}