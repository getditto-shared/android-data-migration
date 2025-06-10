package live.ditto.dittomigrationandroid.util

import live.ditto.DiskUsageItem

fun DiskUsageItem.calculateSizeInMb(path: String): Int {
    val diskUsageItem = this.childItems?.firstOrNull { it.path.endsWith(path) }
    return diskUsageItem?.let {
        it.sizeInBytes / (1024 * 1024)
    } ?: 0
}
