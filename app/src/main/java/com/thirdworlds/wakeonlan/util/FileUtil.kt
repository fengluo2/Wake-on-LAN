package com.thirdworlds.wakeonlan.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.io.File

object FileUtil {
    fun exportToDownloadFile(context: Context, content: String, fileName: String): Uri? {
        val contentResolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            contentResolver.openOutputStream(it)?.use { outputStream ->
                outputStream.write(content.toByteArray())
            }
        }
        return uri
    }

    fun getFilePathFromUri(context: Context, uri: Uri): String? {
        var filePath: String? = null

        // 判断Uri是否为文档Uri
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // 获取文档Id
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":")
            val type = split[0]

            // 如果是主存储（primary），直接获取文件路径
            filePath = if ("primary" == type) {
                File(context.getExternalFilesDir(null), split[1]).path
            } else {
                // 处理其他存储设备的路径（如 SD 卡）
                File(context.getExternalFilesDir(null), split[1]).path
            }
        } else if (uri.scheme == "content") {
            // 如果是 content:// Uri，尝试从 MediaStore 获取路径
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.use {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                if (it.moveToFirst()) {
                    filePath = it.getString(columnIndex)
                }
            }
        } else if (uri.scheme == "file") {
            // 如果是 file:// Uri，直接获取文件路径
            filePath = uri.path
        }

        return filePath
    }
}