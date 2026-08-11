package io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider

import android.content.Context
import android.os.ParcelFileDescriptor
import org.autojs.plugin.yolo.api.YoloOpenSessionRequest
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

internal data class MaterializedModelArtifact(
    val role: String,
    val logicalName: String,
    val file: File,
)

internal class MaterializedModel private constructor(
    private val directory: File,
    val artifacts: List<MaterializedModelArtifact>,
) : Closeable {
    override fun close() {
        artifacts.forEach { runCatching { it.file.delete() } }
        runCatching { directory.delete() }
    }

    companion object {
        fun create(
            context: Context,
            request: YoloOpenSessionRequest,
            descriptors: OwnedParcelFileDescriptors,
            maximumArtifactBytes: Long,
            deadline: YoloSessionOpenDeadline,
        ): MaterializedModel {
            val root = File(context.noBackupFilesDir, "yolo-model-sessions")
            check(root.isDirectory || root.mkdirs()) { "Cannot create private YOLO model root" }
            val directory = File(root, UUID.randomUUID().toString())
            check(directory.mkdir()) { "Cannot create private YOLO model session" }
            val materialized = ArrayList<MaterializedModelArtifact>(request.artifacts.size)
            try {
                request.artifacts.forEachIndexed { index, declaration ->
                    deadline.requireRemaining()
                    val file = File(directory, "$index-${declaration.logicalName}")
                    descriptors.consume(declaration.descriptorIndex) { descriptor ->
                        require(descriptor.statSize == declaration.sizeBytes) {
                            "R1 model descriptors must be regular files with the declared length"
                        }
                        ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { input ->
                            FileOutputStream(file).use { output ->
                                DeclaredDescriptorReader.copyAndVerify(
                                    input = input,
                                    output = output,
                                    declaration = declaration,
                                    maximumLengthBytes = maximumArtifactBytes,
                                    deadline = deadline,
                                    verifyProducerCompletion = descriptor::checkError,
                                )
                                output.fd.sync()
                                deadline.requireRemaining()
                            }
                        }
                    }
                    materialized += MaterializedModelArtifact(
                        role = declaration.role,
                        logicalName = declaration.logicalName,
                        file = file,
                    )
                }
                return MaterializedModel(directory, materialized.toList())
            } catch (error: Throwable) {
                directory.listFiles()?.forEach { runCatching { it.delete() } }
                runCatching { directory.delete() }
                throw error
            } finally {
                descriptors.close()
            }
        }
    }
}
