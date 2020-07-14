package io.github.wechaty.memorycard

import io.github.wechaty.io.github.wechaty.memorycard.backend.StorageFile
import io.github.wechaty.io.github.wechaty.memorycard.backend.StorageNop
import io.github.wechaty.io.github.wechaty.memorycard.backend.StorageObs
import io.github.wechaty.io.github.wechaty.memorycard.backend.StorageS3

sealed class StorageBackendOptions {
    var type: String? = null
}

class StorageNopOptions: StorageBackendOptions() {
    var placeholder: String? = null
}

typealias StorageFileOptions = StorageNopOptions

data class StorageS3Options(
    val accessKeyId:String,
    val secretAccessKey:String,
    val region:String,
    val bucket:String
): StorageBackendOptions()


data class StorageObsOptions(
    val accessKeyId:String,
    val secretAccessKey:String,
    val server:String,
    val bucket:String
): StorageBackendOptions()

val BACKEND_DICT = mapOf(
    "file" to StorageFile::class,
    "s3" to StorageS3::class,
    "nop" to StorageNop::class,
    "obs" to StorageObs::class
)
