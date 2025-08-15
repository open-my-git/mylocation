package akhoi.libs.mlct.tools

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancel
import kotlinx.coroutines.flow.filterNotNull
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.StandardWatchEventKinds.ENTRY_DELETE
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
import java.nio.file.StandardWatchEventKinds.OVERFLOW
import java.nio.file.WatchKey
import java.nio.file.WatchService
import kotlin.io.path.relativeTo

class FileWatcher(private val rootDir: File) {
    private val watchService: WatchService = FileSystems.getDefault().newWatchService()
    private val watchEventMap = mutableMapOf<String, MutableStateFlow<EventData?>>()
    private val watchDirList = mutableListOf<WatchKey>()

    fun watchFile(key: String): Flow<EventData> {
        var eventChannel = watchEventMap[key]
        if (eventChannel == null) {
            eventChannel = MutableStateFlow(null)
            watchEventMap[key] = eventChannel
        }
        return eventChannel.filterNotNull()
    }

    fun unwatchFile(key: String) {
        watchEventMap.remove(key)
    }

    fun watchDirectory(key: String) {
        val dir = rootDir.resolve(key)
        val dirWatchKey =
            dir.toPath().register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY)
        watchDirList.add(dirWatchKey)
    }

    fun unwatchDirectory(key: String) {
        val dir = rootDir.resolve(key)
        watchDirList.removeIf { (it.watchable() as? Path)?.toFile() == dir }
    }

    suspend fun start() {
        var watchKey: WatchKey? = null
        try {
            val rootWatchKey =
                rootDir.toPath().register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY)
            watchDirList.add(rootWatchKey)
            while (true) {
                delay(1000L)
                watchKey = watchService.take() ?: continue
                val watchDirPath = watchKey.watchable() as? Path
                for (event in watchKey.pollEvents()) {
                    val path = (event as? java.nio.file.WatchEvent<Path>)?.context()
                    val kind = event.kind()
                    if (watchDirPath == null || kind == null || path == null || kind == OVERFLOW) {
                        continue
                    }
                    val event = when (kind) {
                        ENTRY_CREATE -> EventType.CREATED
                        ENTRY_DELETE -> EventType.DELETED
                        ENTRY_MODIFY -> EventType.UPDATED
                        else -> continue
                    }
                    val fullPath = watchDirPath.resolve(path)
                    val keyPath = fullPath.relativeTo(rootDir.toPath())
                    watchEventMap[keyPath.toString()]?.emit(EventData(event, keyPath))
                }

                if (!watchKey.reset()) {
                    break
                }
            }
        } catch (_: Exception) {
            // Log.e(TAG, ex.message ?: "")
        } finally {
            watchKey?.cancel()
            stop()
        }
    }

    fun stop() {
        watchEventMap.clear()
        watchDirList.forEach { key -> key.cancel() }
        watchDirList.clear()
        watchService.close()
    }

    enum class EventType {
        CREATED, DELETED, UPDATED
    }

    data class EventData(
        val type: EventType,
        val path: Path
    )
}
