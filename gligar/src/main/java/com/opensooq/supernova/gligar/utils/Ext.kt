package com.opensooq.supernova.gligar.utils

import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.os.Environment
import androidx.paging.PagedList
import java.io.File
import android.os.Environment.DIRECTORY_PICTURES
import android.os.Environment.getExternalStoragePublicDirectory
import android.view.View
import androidx.annotation.IdRes
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Hani AlMomani on 24,September,2019
 */

internal fun Cursor.doWhile(action: () -> Unit) {
    this.use {
        if (this.moveToFirst()) {
            do {
                action()
            } while (this.moveToNext())
        }
    }
}

internal fun getSupportedImagesExt(supportedExt: List<String>): String {
    var result = ""
    return if (supportedExt.isEmpty()) {
        result = ALL_TYPES
        result
    } else {
        supportedExt.forEach {
            result += "$it/"
        }
        result
    }
}

@Throws(IOException::class)
internal fun createTempImageFile(context : Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val storageDir = context.getExternalFilesDir(DIRECTORY_PICTURES)
    val image = File.createTempFile(
        imageFileName,
        ".jpg",
        storageDir
    )
    return image
}