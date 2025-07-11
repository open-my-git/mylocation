package akhoi.libs.tools

import androidx.annotation.VisibleForTesting
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import kotlin.math.min

class ByteSeriesDataStore(locationDir: File, name: String) {
    private val contentFile: File = File("$locationDir/$name")

    fun initialize() {
        contentFile.parentFile?.mkdirs()
    }

    @Synchronized
    fun append(bytes: ByteArray) {
        val fileOutStream = FileOutputStream(contentFile, true)
        try {
            fileOutStream.write(bytes)
        } finally {
            fileOutStream.close()
        }
    }

    @Synchronized
    fun read(offset: Long, limit: Int): ByteArray {
        val contentLength = if (contentFile.exists()) {
            contentFile.length()
        } else {
            0
        }
        if (offset >= contentLength ||
            offset < 0 ||
            limit <= 0 ||
            limit > contentLength
        ) {
            return ByteArray(0)
        }

        val actualLimit = min(limit, maxLimit)
        val resultSize = min(actualLimit, (contentLength - offset).toInt())
        val result = ByteArray(resultSize)
        var resultBytesRead = 0
        val contentChannel = Files.newByteChannel(contentFile.toPath(), StandardOpenOption.READ)
        contentChannel.position(offset)
        val buffer = ByteBuffer.allocate(BUFFER_SIZE)
        try {
            while (resultBytesRead < resultSize) {
                val bytesRead = contentChannel.read(buffer)
                if (bytesRead <= 0) {
                    break
                }
                val bytesToCopy = min(resultSize - resultBytesRead, bytesRead)
                buffer.flip()
                repeat(bytesToCopy) {
                    result[resultBytesRead++] = buffer.get()
                }
                resultBytesRead += bytesToCopy
            }
        } finally {
            contentChannel?.close()
        }

        return result
    }

    @VisibleForTesting
    var maxLimit = MAX_LIMIT

    companion object {
        private const val BUFFER_SIZE = 512
        private const val MAX_LIMIT = 2 * BUFFER_SIZE
    }
}