package live.ditto.dittomigrationandroid.util

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LogUtil {

    private val logMessages = mutableStateListOf<LogMessage>()

    private val _logMessagesFlow = MutableStateFlow<List<LogMessage>>(logMessages)
    val logMessagesFlow = _logMessagesFlow.asStateFlow()

    fun logDebug(message: String) {
        Log.d(TAG, message)
        logMessages.add(
            LogMessage(
                level = LogLevel.DEBUG,
                message = message
            )
        )
        updateFlow()
    }

    fun logError(message: String) {
        Log.e(TAG, message)
        logMessages.add(
            LogMessage(
                level = LogLevel.ERROR,
                message = message
            )
        )
        updateFlow()
    }

    private fun updateFlow() {
        _logMessagesFlow.update {
            logMessages
        }
    }

    companion object {
        private const val TAG = "DittoMigrationLogUtil"
    }
}

data class LogMessage(
    val level: LogLevel,
    val message: String
)

enum class LogLevel {
    DEBUG,
    ERROR
}