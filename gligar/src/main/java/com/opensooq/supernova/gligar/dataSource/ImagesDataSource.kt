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
 */


internal class ImagesDataSource(private val contentResolver: ContentResolver){

    fun loadAlbums(): ArrayList<AlbumItem> {
        val albumCursor = contentResolver.query(
            cursorUri,
            arrayOf(DISPLAY_NAME_COLUMN,MediaStore.Images.ImageColumns.BUCKET_ID),
            null,
            null,
            ORDER_BY
        )
        val list = arrayListOf<AlbumItem>()
        try {
            list.add(AlbumItem("All", true,"0"))
            if (albumCursor == null) {
                return list
            }
            albumCursor.doWhile {
                val bucketId = albumCursor.getString(albumCursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_ID))
                val name = albumCursor.getString(albumCursor.getColumnIndex(DISPLAY_NAME_COLUMN)) ?: bucketId
                var albumItem = AlbumItem(name, false, bucketId)
                if (!list.contains(albumItem)) {
                    list.add(albumItem)
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
            if (albumItem == null || albumItem.isAll) {
                photoCursor = contentResolver.query(
                    cursorUri,
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
                    cursorUri,
                    arrayOf(
                        ID_COLUMN,
                        PATH_COLUMN
                    ),
                    "${MediaStore.Images.ImageColumns.BUCKET_ID} =?",
                    arrayOf(albumItem.bucketId),
                    "$ORDER_BY LIMIT $PAGE_SIZE OFFSET $offset"
                )
            }
            photoCursor?.isAfterLast ?: return list
            photoCursor.doWhile {
                val image = photoCursor.getString((photoCursor.getColumnIndex(PATH_COLUMN)))
                list.add(ImageItem(image, ImageSource.GALLERY, 0))
            }
        } finally {
            if (photoCursor != null && !photoCursor.isClosed()) {
                photoCursor.close()
            }
        }
        return list
    }
}