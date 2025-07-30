package akhoi.libs.tools

import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

class ByteConcatDataStore(locationDir: File, name: String) {
    private val contentFile: File = File("$locationDir/$name")

    init {
        contentFile.parentFile?.mkdirs()
    }

    @Synchronized
    fun append(bytes: ByteArray) {
        FileOutputStream(contentFile, true).use { outStream ->
            var total = 0
            var len: Int
            while (total < bytes.size) {
                len = min(BUFFER_SIZE, bytes.size - total)
                outStream.write(bytes, total, len)
                total += len
            }
            outStream.flush()
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

        val result = mutableListOf<Byte>()
        contentFile.inputStream().use { inpStream ->
            val total = min(limit, (contentLength - offset).toInt())
            val buffer = ByteArray(min(BUFFER_SIZE, total))
            inpStream.skip(offset)
            while (result.size < total) {
                val len = min(total - result.size, buffer.size)
                val read = inpStream.read(buffer, 0, len)
                if (read <= 0) {
                    break
                }
                for (i in 0 until read) {
                    result.add(buffer[i])
                }
            }
        }

        return result.toByteArray()
    }

    fun clear() {
        contentFile.delete()
    }

    companion object Companion {
        private const val BUFFER_SIZE = 1024
    }
}