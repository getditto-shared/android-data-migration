package live.ditto.dittomigrationandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import live.ditto.dittomigrationandroid.DittoMigrationApplication
import live.ditto.dittomigrationandroid.data.DiskUsageMetrics
import live.ditto.dittomigrationandroid.util.LogMessage
import live.ditto.dittomigrationandroid.util.LogUtil

class DataMigrationViewModel(
    private val logUtil: LogUtil,
    private val diskUsageFlow: Flow<DiskUsageMetrics>
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            logUtil.logMessagesFlow.collectLatest { logMessages ->
                _uiState.update { uiState ->
                    uiState.copy(
                        logMessages = logMessages
                    )
                }
            }
        }

        viewModelScope.launch {
            diskUsageFlow.collectLatest { diskUsageMetrics ->
                _uiState.update { uiState ->
                    uiState.copy(
                        diskUsageMetrics = diskUsageMetrics
                    )
                }
            }
        }
    }

    fun startSubscriptions(dittoMigrationApplication: DittoMigrationApplication) {
        viewModelScope.launch {
            dittoMigrationApplication.subscribeToAllCollections()
        }
        _uiState.update {
            it.copy(
                enableStartSubscriptionsButton = false,
                enableRestartSubscriptionsButton = true
            )
        }
    }

    fun restartSubscriptionsWithNewApp(dittoMigrationApplication: DittoMigrationApplication) {
        viewModelScope.launch {
            dittoMigrationApplication.restartDittoWithNewAppId()
        }
        _uiState.update {
            it.copy(
                enableRestartSubscriptionsButton = false
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val dittoMigrationApplication =
                    (checkNotNull(extras[APPLICATION_KEY]) as DittoMigrationApplication)

                return DataMigrationViewModel(
                    logUtil = dittoMigrationApplication.logUtil,
                    diskUsageFlow = dittoMigrationApplication.dittoStoreSizeMbFlow
                ) as T
            }
        }
    }
}

data class UiState(
    val enableStartSubscriptionsButton: Boolean = true,
    val enableRestartSubscriptionsButton: Boolean = false,
    val logMessages: List<LogMessage> = emptyList<LogMessage>(),
    val diskUsageMetrics: DiskUsageMetrics = DiskUsageMetrics()
)
