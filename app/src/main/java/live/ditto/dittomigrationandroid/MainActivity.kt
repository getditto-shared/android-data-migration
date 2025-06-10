package live.ditto.dittomigrationandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import live.ditto.dittomigrationandroid.ui.theme.DittoMigrationAppTheme
import live.ditto.dittomigrationandroid.view.DittoMigrationApp
import live.ditto.dittomigrationandroid.viewmodel.DataMigrationViewModel
import live.ditto.transports.DittoSyncPermissions

class MainActivity : ComponentActivity() {

    private val dittoMigrationApplication by lazy {
        return@lazy applicationContext as DittoMigrationApplication
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            dittoMigrationApplication.ditto.refreshPermissions()
        }

    private val dataMigrationViewModel: DataMigrationViewModel by viewModels { DataMigrationViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val state by dataMigrationViewModel.uiState.collectAsStateWithLifecycle()

            DittoMigrationAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DittoMigrationApp(
                        modifier = Modifier.padding(innerPadding),
                        logMessages = state.logMessages,
                        diskUsageMetrics = state.diskUsageMetrics,
                        isStartSubscriptionsEnabled = state.enableStartSubscriptionsButton,
                        isRestartSubscriptionsEnabled = state.enableRestartSubscriptionsButton,
                        onStartSubscriptionsClicked = {
                                dataMigrationViewModel.startSubscriptions(dittoMigrationApplication)
                        },
                        onRestartSubscriptionsClicked = {
                            dataMigrationViewModel.restartSubscriptionsWithNewApp(dittoMigrationApplication)
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requestMissingPermissions()
    }

    private fun requestMissingPermissions() {
        val missing = DittoSyncPermissions(this).missingPermissions()
        if (missing.isNotEmpty()) {
            requestPermissionLauncher.launch(missing)
        }
    }
}