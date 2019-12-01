package com.opensooq.supernova.gligar.utils

import android.net.Uri
import android.provider.MediaStore

/**
 * Created by Hani AlMomani on 24,September,2019
 */

internal val cursorUri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
internal const val ORDER_BY = MediaStore.Images.Media.DATE_TAKEN + " DESC"
internal const val DISPLAY_NAME_COLUMN = MediaStore.Images.Media.BUCKET_DISPLAY_NAME
internal const val ID_COLUMN = MediaStore.Images.Media._ID
internal const val PATH_COLUMN = MediaStore.Images.Media.DATA
internal const val PAGE_SIZE = 20

internal const val IMAGES = "images"
internal const val ALBUMS = "albums"
internal const val PHOTO_PATH = "photo_path"
internal const val ALBUM_POS = "album_pos"
internal const val PAGE = "page"
internal const val SELECTED_ALBUM = "selected_album"
internal const val SELECTED_IMAGES = "selected_images"
internal const val CURRENT_SELECTION = "curren_selection"
internal const val LIMIT = "limit"
internal const val DISABLE_CAMERA = "limit"
