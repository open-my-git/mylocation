package akhoi.libs.mlct.tools

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class FileWatcherTest {
    private lateinit var fileWatcher: FileWatcher
    private lateinit var rootDir: File

    @Before
    fun setup() {
        rootDir = File("akhoi.libs.mlct.tools.FileWatcherTest")
        rootDir.deleteRecursively()
        rootDir.mkdirs()
        fileWatcher = FileWatcher(rootDir)
    }

    @Test
    fun test() = runTest {
        val flow = fileWatcher.watchFile("test_dir/test_file")
        backgroundScope.launch {
            fileWatcher.start()
        }
        val collectionJob = launch {
            flow.onCompletion {
                val i = 0
            }
                .collectIndexed { i, data ->
                val j = i
            }
        }
        testScheduler.runCurrent()
        val testDir = rootDir.resolve("test_dir")
        testDir.mkdirs()
        fileWatcher.watchDirectory("test_dir")
        val testFile = testDir.resolve("test_file")
        testFile.createNewFile()
        testScheduler.advanceTimeBy(2100L)
        testFile.delete()
        testScheduler.advanceTimeBy(1000L)
        fileWatcher.stop()
        collectionJob.cancel()
    }

    @After
    fun tearDown() {
        rootDir.deleteRecursively()
    }

}