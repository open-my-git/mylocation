package akhoi.libs.mlct.tools

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import java.io.File
import java.nio.file.ClosedWatchServiceException
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.nio.file.WatchService
import kotlin.io.path.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FileWatcherTest {
    private lateinit var fileWatcher: FileWatcher
    private lateinit var rootDir: File
    private lateinit var spyWatchService: WatchService

    @Before
    fun setup() {
        rootDir = File("akhoi.libs.mlct.tools.FileWatcherTest")
        rootDir.deleteRecursively()
        rootDir.mkdirs()
        spyWatchService = spy(FileSystems.getDefault().newWatchService())
        fileWatcher = FileWatcher(rootDir, spyWatchService)
    }

    @Test
    fun testWatchFile_singleEvent() = runTest {
        val watchFlow = fileWatcher.watchFile("test_dir/test_file")

        val mockWatchKey = mock<WatchKey>()
        whenever(mockWatchKey.watchable()).thenReturn(rootDir.resolve("test_dir").toPath())
        whenever(mockWatchKey.reset()).thenReturn(true)

        val mockEvent = mock<WatchEvent<Path>>()
        whenever(mockEvent.context()).thenReturn(Path("test_file"))
        whenever(mockEvent.kind()).thenReturn(ENTRY_CREATE)

        whenever(mockWatchKey.pollEvents()).thenReturn(listOf(mockEvent))
        whenever(spyWatchService.poll()).thenReturn(mockWatchKey)

        backgroundScope.launch {
            fileWatcher.start()
        }

        runCurrent()

        val eventData = watchFlow.first()
        assertEquals(
            FileWatcher.EventData(
                FileWatcher.EventType.CREATED,
                Path("test_dir/test_file")
            ),
            eventData
        )

        fileWatcher.stop()
        whenever(spyWatchService.poll()).thenThrow(ClosedWatchServiceException())
    }

    @Test
    fun testRegisterDirectory_directoryExisted() = runTest {
        val testDir = rootDir.resolve("existed_test_dir")
        testDir.mkdir()

        val isSucceeded = fileWatcher.registerDirectory("existed_test_dir")
        assertTrue(isSucceeded)
    }

    @Test
    fun testRegisterDirectory_directoryNotExisted() = runTest {
        val isSucceeded = fileWatcher.registerDirectory("not_existed_test_dir")
        assertFalse(isSucceeded)
    }

    @After
    fun tearDown() {
        rootDir.deleteRecursively()
    }
}