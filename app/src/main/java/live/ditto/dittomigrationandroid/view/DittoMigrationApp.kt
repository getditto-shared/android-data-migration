package live.ditto.dittomigrationandroid.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import live.ditto.dittomigrationandroid.data.DiskUsageMetrics
import live.ditto.dittomigrationandroid.util.LogLevel
import live.ditto.dittomigrationandroid.util.LogMessage


@Composable
fun DittoMigrationApp(
    modifier: Modifier = Modifier,
    logMessages: List<LogMessage>,
    diskUsageMetrics: DiskUsageMetrics,
    isStartSubscriptionsEnabled: Boolean,
    isRestartSubscriptionsEnabled: Boolean,
    onStartSubscriptionsClicked: () -> Unit,
    onRestartSubscriptionsClicked: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        MigrationControls(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(.30f),
            isStartSubscriptionsEnabled = isStartSubscriptionsEnabled,
            isRestartSubscriptionsEnabled = isRestartSubscriptionsEnabled,
            onStartSubscriptionsClicked = { onStartSubscriptionsClicked() },
            onRestartSubscriptionsClicked = { onRestartSubscriptionsClicked() }
        )
        DiskUsageMetrics(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            diskUsageMetrics = diskUsageMetrics
        )
        LogOutput(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 4.dp, end = 4.dp),
            logMessages = logMessages
        )
    }
}

@Composable
private fun MigrationControls(
    modifier: Modifier = Modifier,
    isStartSubscriptionsEnabled: Boolean,
    isRestartSubscriptionsEnabled: Boolean,
    onStartSubscriptionsClicked: () -> Unit,
    onRestartSubscriptionsClicked: () -> Unit
) {
    Column(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Ditto Data Migration App",
            style = MaterialTheme.typography.titleLarge
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = isStartSubscriptionsEnabled,
            onClick = {
                onStartSubscriptionsClicked()
            }
        ) {
            Text(
                text = "Start subscriptions",
                style = MaterialTheme.typography.labelMedium
            )
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = isRestartSubscriptionsEnabled,
            onClick = {
                onRestartSubscriptionsClicked()
            }
        ) {
            Text(
                text = "Restart subscriptions with new app",
                style = MaterialTheme.typography.labelMedium
            )
        }

    }
}

@Composable
private fun DiskUsageMetrics(
    modifier: Modifier = Modifier,
    diskUsageMetrics: DiskUsageMetrics
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "Disk store size: ${diskUsageMetrics.storeSizeInMb} MB")
        Text(text = "Replication size: ${diskUsageMetrics.replicationSizeInMb} MB")
    }
}

@Composable
private fun LogOutput(
    modifier: Modifier = Modifier,
    logMessages: List<LogMessage>
) {
    val listState = rememberLazyListState()
    val isUserScrolling by remember(listState) {
        derivedStateOf {
            listState.isScrollInProgress
        }
    }
    val shouldAutoScroll by remember(listState) {
        derivedStateOf {
            // Auto-scroll if not currently user-scrolling OR if the last visible item is close to the end
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) {
                true // If no items are visible (e.g. list is empty), auto-scroll when items appear
            } else {
                // Check if the last visible item is the last item in the list or very close to it
                // This threshold can be adjusted.
                val lastVisibleItemIndex = visibleItemsInfo.lastOrNull()?.index ?: -1
                val totalItemsCount = layoutInfo.totalItemsCount
                lastVisibleItemIndex >= totalItemsCount - 2 // Auto-scroll if last or second to last item is visible
            }
        }
    }
    val totalItemsCount by remember(listState) {
        derivedStateOf { listState.layoutInfo.totalItemsCount }
    }

    LazyColumn(
        modifier = modifier,
        state = listState
    ) {
        items(logMessages) { logMessage ->
            val color = when (logMessage.level) {
                LogLevel.DEBUG -> MaterialTheme.colorScheme.primary
                LogLevel.ERROR -> MaterialTheme.colorScheme.error
            }
            Text(
                text = "[${logMessage.level}] ${logMessage.message}",
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
        }
    }

    LaunchedEffect(logMessages.size, totalItemsCount) {
        // Only scroll if new items were actually added to the list and we should auto-scroll
        if (logMessages.isNotEmpty() && (shouldAutoScroll || !isUserScrolling)) {
            // Animate scroll to the last item.
            // listState.layoutInfo.totalItemsCount gives the current number of items
            // that the LazyColumn is aware of (composed items).
            // logMessages.size is the total number of items in your data source.
            // We want to scroll to the end of the data source.
            val targetIndex = logMessages.size - 1
            if (targetIndex >= 0) { // Ensure there's at least one item
                listState.animateScrollToItem(index = targetIndex)
            }
        }
    }
}