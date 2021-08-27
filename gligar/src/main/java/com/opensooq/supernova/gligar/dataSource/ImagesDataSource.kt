package com.opensooq.supernova.gligar.dataSource

import android.content.ContentResolver
import android.database.Cursor
import android.provider.MediaStore
import androidx.paging.PositionalDataSource
import com.opensooq.OpenSooq.ui.imagePicker.model.AlbumItem
import com.opensooq.OpenSooq.ui.imagePicker.model.ImageItem
import com.opensooq.OpenSooq.ui.imagePicker.model.ImageSource
import com.opensooq.supernova.gligar.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.coroutines.CoroutineContext

/**
 * Created by Hani AlMomani on 24,September,2019
   Modified by Omkar Tenkale to apply fix https://github.com/OpenSooq/Gligar/issues/23#issuecomment-798877787 by Shay-BH for issue https://github.com/OpenSooq/Gligar/issues/23 
 */


internal class ImagesDataSource(private val contentResolver: ContentResolver){

fun getCursorUri(): Uri {

    val collection: Uri
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        collection = newCursorUri;
    } else {
        collection = cursorUri;
    }

    return collection;
}

fun loadAlbums(): ArrayList<AlbumItem> {
    val albumCursor = contentResolver.query(
            getCursorUri(),
            arrayOf(DISPLAY_NAME_COLUMN, MediaStore.Images.ImageColumns.BUCKET_ID),
            null,
            null,
            ORDER_BY
    )
    val list = arrayListOf<AlbumItem>()
    try {
        list.add(AlbumItem("All", true, "0"))
        if (albumCursor == null) {
            return list
        }
        albumCursor.doWhile {
            try {
                val bucketId = albumCursor.getString(albumCursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_ID))
                val name = albumCursor.getString(albumCursor.getColumnIndex(DISPLAY_NAME_COLUMN))
                        ?: bucketId
                var albumItem = AlbumItem(name, false, bucketId)
                if (!list.contains(albumItem)) {
                    list.add(albumItem)
                }
            }
            catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    } finally {
        if (albumCursor != null && !albumCursor.isClosed) {
            albumCursor.close()
        }
    }
    return list
}

fun loadAlbumImages(
        albumItem: AlbumItem?,
        page: Int
): ArrayList<ImageItem> {
    val offset = page * PAGE_SIZE
    val list: ArrayList<ImageItem> = arrayListOf()
    var photoCursor: Cursor? = null
    try {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {

            val bundle = Bundle().apply {
                putInt(ContentResolver.QUERY_ARG_LIMIT, PAGE_SIZE)
                putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
                putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, "${MediaStore.MediaColumns.DATE_MODIFIED} DESC")
            }

            photoCursor = contentResolver.query(
                getCursorUri(),
                arrayOf(
                    ID_COLUMN,
                    PATH_COLUMN
                ),
                bundle,
                null
            )
        }
        else {
            if (albumItem == null || albumItem.isAll) {
                photoCursor = contentResolver.query(
                    getCursorUri(),
                    arrayOf(
                        ID_COLUMN,
                        PATH_COLUMN
                    ),
                    null,
                    null,
                    "$ORDER_BY LIMIT $PAGE_SIZE OFFSET $offset"
                )
            } else {
                photoCursor = contentResolver.query(
                    getCursorUri(),
                    arrayOf(
                        ID_COLUMN,
                        PATH_COLUMN
                    ),
                    "${MediaStore.Images.ImageColumns.BUCKET_ID} =?",
                    arrayOf(albumItem.bucketId),
                    "$ORDER_BY LIMIT $PAGE_SIZE OFFSET $offset"
                )
            }
        }
        photoCursor?.isAfterLast ?: return list
        photoCursor.doWhile {
            try {
                val image = photoCursor.getString((photoCursor.getColumnIndex(PATH_COLUMN)))
                list.add(ImageItem(image, ImageSource.GALLERY, 0))
            }
            catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    } finally {
        if (photoCursor != null) {
            if (!photoCursor.isClosed()) {
                photoCursor.close()
            }
        }
    }
    return list
}

}
