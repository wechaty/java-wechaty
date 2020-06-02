package io.github.wechaty.memorycard

sealed class StorageBackendOptions{
    var type:String?=null
}

data class StorageS3Options(
    val accessKeyId:String,
    val secretAccessKey:String,
    val region:String,
    val bucket:String
):StorageBackendOptions()


data class StorageObsOptions(
    val accessKeyId:String,
    val secretAccessKey:String,
    val server:String,
    val bucket:String
):StorageBackendOptions()

class StorageNopOptions: StorageBackendOptions() {
    var placeholder: String? = null
}

typealias StorageFileOptions = StorageNopOptions

val BACKEND_DICT = mapOf(
    "file" to StorageFile::class
)
