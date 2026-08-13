package io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.attribute.BasicFileAttributes

class StaleModelSessionCleanupTest {
    @Test
    fun removesNestedCrashResidueAndKeepsOnlyTheFixedRoot() {
        withTemporaryDirectory { noBackup ->
            val root = File(noBackup, YOLO_MODEL_SESSION_DIRECTORY)
            val session = File(root, "stale-session")
            assertTrue(session.mkdirs())
            File(session, "0-model.ncnn.param").writeText("param")
            File(session, "1-model.ncnn.bin").writeText("model")

            val cleaned = StaleModelSessionCleanup.clean(noBackup, JvmFileSystem)

            assertEquals(root.canonicalFile, cleaned)
            assertTrue(cleaned.isDirectory)
            assertTrue(cleaned.listFiles().orEmpty().isEmpty())
        }
    }

    @Test
    fun refusesSymbolicEntryWithoutTouchingAnythingOutsideTheRoot() {
        withTemporaryDirectory { noBackup ->
            val root = File(noBackup, YOLO_MODEL_SESSION_DIRECTORY)
            val suspicious = File(root, "suspicious")
            val outside = File(noBackup.parentFile, "outside-${System.nanoTime()}")
            assertTrue(suspicious.mkdirs())
            assertTrue(outside.mkdir())
            val marker = File(outside, "must-survive").apply { writeText("safe") }
            val fileSystem = object : ModelSessionFileSystem by JvmFileSystem {
                override fun type(file: File): ModelSessionEntryType =
                    if (file.absoluteFile == suspicious.absoluteFile) {
                        ModelSessionEntryType.SYMBOLIC_LINK
                    } else {
                        JvmFileSystem.type(file)
                    }
            }

            try {
                val failure = runCatching {
                    StaleModelSessionCleanup.clean(noBackup, fileSystem)
                }.exceptionOrNull()

                assertTrue(failure is IllegalStateException)
                assertTrue(suspicious.exists())
                assertTrue(marker.isFile)
            } finally {
                marker.delete()
                outside.delete()
            }
        }
    }

    @Test
    fun failsClosedWhenAStaleEntryCannotBeDeleted() {
        withTemporaryDirectory { noBackup ->
            val root = File(noBackup, YOLO_MODEL_SESSION_DIRECTORY)
            assertTrue(root.mkdir())
            val stale = File(root, "stale.bin").apply { writeText("model") }
            val fileSystem = object : ModelSessionFileSystem by JvmFileSystem {
                override fun delete(file: File): Boolean =
                    if (file.absoluteFile == stale.absoluteFile) false else JvmFileSystem.delete(file)
            }

            val failure = runCatching {
                StaleModelSessionCleanup.clean(noBackup, fileSystem)
            }.exceptionOrNull()

            assertTrue(failure is IllegalStateException)
            assertTrue(stale.isFile)
        }
    }

    private fun withTemporaryDirectory(block: (File) -> Unit) {
        val directory = Files.createTempDirectory("yolo-stale-cleanup-").toFile()
        try {
            block(directory)
        } finally {
            directory.walkBottomUp().forEach(File::delete)
            assertFalse(directory.exists())
        }
    }

    private object JvmFileSystem : ModelSessionFileSystem {
        override fun canonical(file: File): File = file.canonicalFile

        override fun type(file: File): ModelSessionEntryType {
            val path = file.toPath()
            if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
                return ModelSessionEntryType.MISSING
            }
            val attributes = Files.readAttributes(
                path,
                BasicFileAttributes::class.java,
                LinkOption.NOFOLLOW_LINKS,
            )
            return when {
                attributes.isSymbolicLink -> ModelSessionEntryType.SYMBOLIC_LINK
                attributes.isDirectory -> ModelSessionEntryType.DIRECTORY
                attributes.isRegularFile -> ModelSessionEntryType.REGULAR_FILE
                else -> ModelSessionEntryType.OTHER
            }
        }

        override fun listDirectory(directory: File): List<File> =
            directory.listFiles()?.toList() ?: error("Cannot list test directory")

        override fun createDirectory(directory: File): Boolean = directory.mkdir()

        override fun delete(file: File): Boolean = file.delete()
    }
}
