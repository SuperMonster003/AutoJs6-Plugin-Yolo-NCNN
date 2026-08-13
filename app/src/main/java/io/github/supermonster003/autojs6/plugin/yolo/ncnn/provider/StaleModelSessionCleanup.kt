package io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider

import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import java.io.File
import java.io.IOException

internal const val YOLO_MODEL_SESSION_DIRECTORY = "yolo-model-sessions"

/**
 * Removes private model copies left by a provider-process crash before Binder is published.
 *
 * The root itself is retained. Any ambiguous filesystem state is rejected instead of being
 * followed or deleted, so cleanup cannot escape the fixed no-backup directory.
 */
internal object StaleModelSessionCleanup {
    fun clean(
        noBackupFilesDir: File,
        fileSystem: ModelSessionFileSystem = AndroidModelSessionFileSystem,
    ): File {
        val trustedBase = fileSystem.canonical(noBackupFilesDir)
        check(fileSystem.type(trustedBase) == ModelSessionEntryType.DIRECTORY) {
            "Private no-backup directory is unavailable"
        }

        val root = File(trustedBase, YOLO_MODEL_SESSION_DIRECTORY).absoluteFile
        check(root.parentFile == trustedBase && root.name == YOLO_MODEL_SESSION_DIRECTORY) {
            "YOLO model root is not the fixed private child"
        }

        when (fileSystem.type(root)) {
            ModelSessionEntryType.MISSING -> {
                check(fileSystem.createDirectory(root)) {
                    "Cannot create private YOLO model root"
                }
            }
            ModelSessionEntryType.DIRECTORY -> Unit
            ModelSessionEntryType.SYMBOLIC_LINK -> error(
                "Private YOLO model root must not be a symbolic link",
            )
            ModelSessionEntryType.REGULAR_FILE,
            ModelSessionEntryType.OTHER,
            -> error("Private YOLO model root is not a directory")
        }

        requireContainedDirectory(root, trustedBase, fileSystem)
        fileSystem.listDirectory(root).forEach { child ->
            deleteContained(child, root, fileSystem)
        }
        check(fileSystem.listDirectory(root).isEmpty()) {
            "Private YOLO model root is not empty after stale-session cleanup"
        }
        return root
    }

    private fun deleteContained(
        entry: File,
        root: File,
        fileSystem: ModelSessionFileSystem,
    ) {
        when (fileSystem.type(entry)) {
            ModelSessionEntryType.SYMBOLIC_LINK -> error(
                "Refusing to follow a symbolic link in the private YOLO model root",
            )
            ModelSessionEntryType.MISSING -> error(
                "YOLO model cleanup entry disappeared unexpectedly",
            )
            ModelSessionEntryType.OTHER -> error(
                "Refusing to remove a special file from the private YOLO model root",
            )
            ModelSessionEntryType.DIRECTORY -> {
                requireContainedDirectory(entry, root, fileSystem)
                fileSystem.listDirectory(entry).forEach { child ->
                    deleteContained(child, root, fileSystem)
                }
            }
            ModelSessionEntryType.REGULAR_FILE -> requireContained(entry, root, fileSystem)
        }

        check(fileSystem.delete(entry)) {
            "Cannot remove stale YOLO model entry"
        }
        check(fileSystem.type(entry) == ModelSessionEntryType.MISSING) {
            "Stale YOLO model entry remains after deletion"
        }
    }

    private fun requireContainedDirectory(
        directory: File,
        rootOrParent: File,
        fileSystem: ModelSessionFileSystem,
    ) {
        check(fileSystem.type(directory) == ModelSessionEntryType.DIRECTORY) {
            "YOLO model cleanup path is not a directory"
        }
        requireContained(directory, rootOrParent, fileSystem, allowSame = true)
    }

    private fun requireContained(
        entry: File,
        root: File,
        fileSystem: ModelSessionFileSystem,
        allowSame: Boolean = false,
    ) {
        val canonicalRoot = fileSystem.canonical(root)
        val canonicalEntry = fileSystem.canonical(entry)
        val prefix = canonicalRoot.path + File.separator
        check((allowSame && canonicalEntry == canonicalRoot) || canonicalEntry.path.startsWith(prefix)) {
            "YOLO model cleanup path escapes its private root"
        }
        check(canonicalEntry == entry.absoluteFile) {
            "YOLO model cleanup path resolves through an unexpected alias"
        }
    }
}

internal enum class ModelSessionEntryType {
    MISSING,
    DIRECTORY,
    REGULAR_FILE,
    SYMBOLIC_LINK,
    OTHER,
}

internal interface ModelSessionFileSystem {
    fun canonical(file: File): File

    fun type(file: File): ModelSessionEntryType

    fun listDirectory(directory: File): List<File>

    fun createDirectory(directory: File): Boolean

    fun delete(file: File): Boolean
}

private object AndroidModelSessionFileSystem : ModelSessionFileSystem {
    override fun canonical(file: File): File = file.canonicalFile

    override fun type(file: File): ModelSessionEntryType = try {
        val mode = Os.lstat(file.absolutePath).st_mode
        when {
            OsConstants.S_ISLNK(mode) -> ModelSessionEntryType.SYMBOLIC_LINK
            OsConstants.S_ISDIR(mode) -> ModelSessionEntryType.DIRECTORY
            OsConstants.S_ISREG(mode) -> ModelSessionEntryType.REGULAR_FILE
            else -> ModelSessionEntryType.OTHER
        }
    } catch (error: ErrnoException) {
        if (error.errno == OsConstants.ENOENT) {
            ModelSessionEntryType.MISSING
        } else {
            throw IOException("Cannot inspect private YOLO model path", error)
        }
    }

    override fun listDirectory(directory: File): List<File> =
        directory.listFiles()?.toList()
            ?: throw IOException("Cannot enumerate private YOLO model directory")

    override fun createDirectory(directory: File): Boolean = directory.mkdir()

    override fun delete(file: File): Boolean = file.delete()
}
