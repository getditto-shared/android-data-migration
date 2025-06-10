package live.ditto.dittomigrationandroid

import android.app.Application
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import live.ditto.Ditto
import live.ditto.DittoIdentity
import live.ditto.DittoLogLevel
import live.ditto.DittoLogger
import live.ditto.android.AndroidDittoDependencies
import live.ditto.android.DefaultAndroidDittoDependencies
import live.ditto.dittomigrationandroid.data.DiskUsageMetrics
import live.ditto.dittomigrationandroid.util.LogUtil
import live.ditto.dittomigrationandroid.util.calculateSizeInMb
import live.ditto.transports.DittoTransportConfig
import java.io.File

class DittoMigrationApplication : Application() {

    lateinit var ditto: Ditto
        private set

    private val defaultAndroidDittoDependencies = DefaultAndroidDittoDependencies(this)

    val logUtil = LogUtil()

    private val _dittoStoreSizeMb = MutableStateFlow<DiskUsageMetrics>(DiskUsageMetrics())
    val dittoStoreSizeMbFlow = _dittoStoreSizeMb.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        ditto = initDitto(
            appId = BuildConfig.EXISTING_APP_ID,
            token = BuildConfig.EXISTING_PLAYGROUND_TOKEN,
            webSocketUrl = BuildConfig.EXISTING_WEBSOCKET_URL,
            authUrl = BuildConfig.EXISTING_AUTH_URL
        )
        startDittoSync()

        ditto.diskUsage.observe { diskUsageItem ->
            _dittoStoreSizeMb.update {
                val storeSize = diskUsageItem.calculateSizeInMb("ditto_store")
                val replicationSize = diskUsageItem.calculateSizeInMb("ditto_replication")
                it.copy(
                    storeSizeInMb = storeSize,
                    replicationSizeInMb = replicationSize
                )
            }
        }
    }

    suspend fun subscribeToAllCollections() {
        logUtil.logDebug("Attempting to subscribe to all collections")
        getAllCollectionNames().forEach { collectionName ->
            logUtil.logDebug("Starting subscription for $collectionName")
            ditto.sync.registerSubscription("SELECT * FROM $collectionName")
        }
    }

    suspend fun restartDittoWithNewAppId() {
        logUtil.logDebug(
            """
            *** Restarting Ditto with new App ID
            ---------
        """.trimIndent()
        )
        closeDitto()
        ditto = initDitto(
            appId = BuildConfig.NEW_APP_ID,
            token = BuildConfig.NEW_PLAYGROUND_TOKEN,
            webSocketUrl = BuildConfig.NEW_WEBSOCKET_URL,
            authUrl = BuildConfig.NEW_AUTH_URL
        )
        startDittoSync()
        subscribeToAllCollections()
    }

    private fun closeDitto() {
        logUtil.logDebug("Stopping Ditto sync")
        ditto.close()
    }

    private fun initDitto(
        appId: String,
        token: String,
        webSocketUrl: String,
        authUrl: String
    ): Ditto {
        logUtil.logDebug(
            """
            Attempting to init Ditto with:
            appId: $appId
            token: $token
            websocket URL: $webSocketUrl
        """.trimIndent()
        )

        val androidDittoDependencies = object : AndroidDittoDependencies {
            override fun context(): Context {
                return this@DittoMigrationApplication
            }

            override fun ensureDirectoryExists(path: String) {
                defaultAndroidDittoDependencies.ensureDirectoryExists(path)
            }

            override fun persistenceDirectory(): String {
                return File(applicationContext.filesDir, "ditto_migration").path
            }

        }

        val identity = DittoIdentity.OnlinePlayground(
            dependencies = androidDittoDependencies,
            appId = appId,
            token = token,
            enableDittoCloudSync = false,
            customAuthUrl = authUrl
        )

        DittoLogger.minimumLogLevel = DittoLogLevel.DEBUG

        return Ditto(dependencies = androidDittoDependencies, identity = identity).apply {
            disableSyncWithV3()
            /**
             * Creating a new transport config, as opposed to updating the existing one creates a
             * transport config with P2P transports disabled by default (so we should only sync
             * with big peer)
             */
            this.transportConfig = DittoTransportConfig().apply {
                connect.websocketUrls.add(webSocketUrl)
            }
        }
    }

    private fun startDittoSync() {
        logUtil.logDebug("Starting Ditto sync and subscribing to __collections")
        ditto.startSync()
        startCollectionSubscription()

        ditto.presence.observe {
            val isConnectedToCloud = it.localPeer.isConnectedToDittoCloud
            if (isConnectedToCloud) {
                logUtil.logDebug("is connected to cloud")
            } else {
                logUtil.logError("is NOT connected to cloud!!")
            }
        }
    }

    /**
     * Get a list of all collection names, excluding internal collections (collections that start
     * with "__").
     */
    private suspend fun getAllCollectionNames(): List<String> {
        logUtil.logDebug("Retrieving all collection names")
        val result = ditto.store.execute("SELECT * FROM __collections")
        val collectionNames = result.items.map {
            it.value["name"] as String
        }.filterNot { it.startsWith("__") }

        if (collectionNames.isNotEmpty()) {
            logUtil.logDebug(
                """
                Found ${collectionNames.size} collection(s):
                $collectionNames
            """.trimIndent()
            )
        } else {
            logUtil.logError("Did not find any non internal collections!")
        }

        return collectionNames
    }

    private fun startCollectionSubscription() {
        ditto.sync.registerSubscription("SELECT * FROM __collections")
    }

}
