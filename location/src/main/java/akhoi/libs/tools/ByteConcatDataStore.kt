package akhoi.libs.tools

import androidx.annotation.VisibleForTesting
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import kotlin.math.min

class ByteConcatDataStore(locationDir: File, name: String) {
    private val contentFile: File = File("$locationDir/$name")

    init {
        contentFile.parentFile?.mkdirs()
    }

    @Synchronized
    fun append(bytes: ByteArray) {
        val bufferedOutStream =
            BufferedOutputStream(FileOutputStream(contentFile, true), BUFFER_SIZE)
        try {
            bufferedOutStream.write(bytes)
        } finally {
            bufferedOutStream.close()
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
        while (resultBytesRead < resultSize) {
            val bytesRead = try {
                contentChannel.read(buffer)
            } finally {
                contentChannel?.close()
            }
            if (bytesRead <= 0) {
                break
            }
            val bytesToCopy = min(resultSize - resultBytesRead, bytesRead)
            buffer.flip()
            buffer.get(result, resultBytesRead, bytesToCopy)
            resultBytesRead += bytesToCopy
        }

        return result
    }

    fun clear() {
        contentFile.delete()
    }

    @VisibleForTesting
    var maxLimit = MAX_LIMIT

    companion object Companion {
        private const val BUFFER_SIZE = 512
        private const val MAX_LIMIT = 2 * BUFFER_SIZE
    }
}