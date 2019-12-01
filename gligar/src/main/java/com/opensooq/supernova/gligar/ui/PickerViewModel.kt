package com.opensooq.supernova.gligar.ui

import android.content.ContentResolver
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opensooq.OpenSooq.ui.imagePicker.model.AlbumItem
import com.opensooq.OpenSooq.ui.imagePicker.model.ImageItem
import com.opensooq.OpenSooq.ui.imagePicker.model.ImageSource
import com.opensooq.supernova.gligar.dataSource.ImagesDataSource
import com.opensooq.supernova.gligar.ui.ImagePickerActivity.Companion.EXTRA_LIMIT
import com.opensooq.supernova.gligar.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Hani AlMomani on 24,September,2019
 */


internal class PickerViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {


    internal fun init(contentResolver: ContentResolver) {
        this.contentResolver = contentResolver
        this.mImageDataSource = ImagesDataSource(this.contentResolver)
    }

    internal var mDoneEnabled = MutableLiveData<Boolean>()
    internal var mDirectCamera = MutableLiveData<Boolean>()
    internal var showOverLimit = MutableLiveData<Boolean>()
    internal var mNotifyPosition = MutableLiveData<Int>()
    internal var mNotifyInsert = MutableLiveData<Int>()
    internal var mAlbums = MutableLiveData<ArrayList<AlbumItem>>()
    internal var mLastAddedImages = MutableLiveData<ArrayList<ImageItem>>()
    internal var saveStateImages = arrayListOf<ImageItem>()
    internal var dumpImagesList = getDumItems()
    internal var mCurrentPhotoPath: String? = null
    internal var mCurrentSelectedAlbum = 0
    internal var mPage = 0

    private var mSelectedAlbum: AlbumItem? = null
    private var mSelectedList = hashMapOf<String, ImageItem>()
    private var mCurrentSelection: Int = 0
    private var mLimit = 0
    private var mCameraCisabled: Boolean = true

    private lateinit var mImageDataSource: ImagesDataSource
    private lateinit var contentResolver: ContentResolver
    private fun getCurrentSelection() = mCurrentSelection

    internal fun isOverLimit() = mCurrentSelection >= mLimit

    internal fun bindArguments(extras: Bundle?) {
        if (extras == null) {
            return
        }
        mLimit = extras.getInt(EXTRA_LIMIT, 0)
        mCameraCisabled = extras.getBoolean(ImagePickerActivity.EXTRA_DISABLE_CAMERA, false)
        mDirectCamera.value = extras.getBoolean(ImagePickerActivity.EXTRA_CAMERA_DIRECT, false)
    }


    internal fun loadAlbums() {
        if (!mAlbums.value.isNullOrEmpty()) {
            return
        }
        viewModelScope.launch {
            val albums = getAlbums()
            mAlbums.value = albums
            loadImages()
        }
    }

    internal fun loadMoreImages() {
        loadImages(true)

    }

    private fun loadImages(isLoadMore: Boolean = false) {
        if (isLoadMore) {
            mPage += 1
        } else {
            mPage = 0
        }
        viewModelScope.launch() {
            val images = getImages()
            if (!isLoadMore && !mCameraCisabled) {
                images.add(0, ImageItem("", ImageSource.CAMERA, 0))
            }
            mLastAddedImages.value = images
        }
    }

    private suspend fun getImages() = withContext(Dispatchers.Default) {
        mImageDataSource.loadAlbumImages(mSelectedAlbum, mPage)
    }


    private suspend fun getAlbums() = withContext(Dispatchers.Default) {
        mImageDataSource.loadAlbums()
    }

    internal fun addCameraItem(adapterItems: ArrayList<ImageItem>?) {
        if (mCurrentPhotoPath.isNullOrEmpty()) {
            return
        }
        val imageItem =
            ImageItem(mCurrentPhotoPath!!, ImageSource.GALLERY, getCurrentSelectionCountForCamera())
        mSelectedList[imageItem.imagePath] = imageItem
        adapterItems?.add(1, imageItem)
        mNotifyInsert.value = 1
    }


    internal fun setImageSelection(position: Int, adapterImageItem: ArrayList<ImageItem>?) {
        if (adapterImageItem.isNullOrEmpty()) {
            return
        }
        val imageItem = adapterImageItem[position]

        if (adapterImageItem[position].source == ImageSource.DUM) {
            return
        }

        if (imageItem.selected == 0) {
            if (isOverLimit()) {
                showOverLimit.value = true
                return
            }
            mCurrentSelection++
            imageItem.selected = mCurrentSelection
            mSelectedList[imageItem.imagePath] = imageItem
        } else {
            for ((i, mItem) in adapterImageItem.withIndex()) {
                if (mItem.selected > imageItem.selected) {
                    mItem.selected--
                    mNotifyPosition.value = i
                }
            }
            imageItem.selected = 0
            mCurrentSelection--
            mSelectedList.remove(imageItem.imagePath)
        }
        mNotifyPosition.value = position
        mDoneEnabled.value = getCurrentSelection() > 0
    }


    private fun getCurrentSelectionCountForCamera(): Int {
        mCurrentSelection++
        return mCurrentSelection
    }


    internal fun getSelectedPaths(): Array<String> {
        val sortedList = mSelectedList.values.sortedWith(compareByDescending { it.selected })
        val pathsList = mutableListOf<String>()
        for (imageItem in sortedList) {
            pathsList.add(imageItem.imagePath)
        }
        return pathsList.toTypedArray()
    }

    private fun getDumItems(): ArrayList<ImageItem> {
        val list = arrayListOf<ImageItem>()
        for (x in 0..PAGE_SIZE) list.add(ImageItem("", ImageSource.DUM, 0))
        return list
    }

    internal fun onAlbumChanged(item: AlbumItem?, pos: Int) {
        mSelectedAlbum = item
        mSelectedList.clear()
        mCurrentSelection = 0
        mCurrentSelectedAlbum = pos
        loadImages()
    }

    internal fun saveState() {
        savedStateHandle.set(IMAGES, saveStateImages)
        savedStateHandle.set(ALBUMS, mAlbums.value)
        savedStateHandle.set(PHOTO_PATH, mCurrentPhotoPath)
        savedStateHandle.set(ALBUM_POS, mCurrentSelectedAlbum)
        savedStateHandle.set(PAGE, mPage)
        savedStateHandle.set(SELECTED_ALBUM, mSelectedAlbum)
        savedStateHandle.set(SELECTED_IMAGES, mSelectedList)
        savedStateHandle.set(CURRENT_SELECTION, mCurrentSelection)
        savedStateHandle.set(LIMIT, mLimit)
        savedStateHandle.set(DISABLE_CAMERA, mCameraCisabled)

    }

    internal fun loadSaveState() {
        saveStateImages = savedStateHandle.get(IMAGES) ?: arrayListOf()
        mAlbums.value = savedStateHandle.get(ALBUMS) ?: arrayListOf()
        mCurrentPhotoPath = savedStateHandle.get(PHOTO_PATH)
        mCurrentSelectedAlbum = savedStateHandle.get(ALBUM_POS) ?: 0
        mPage = savedStateHandle.get(PAGE) ?: 0
        mSelectedAlbum = savedStateHandle.get(SELECTED_ALBUM)
        mSelectedList = savedStateHandle.get(SELECTED_IMAGES) ?: hashMapOf()
        mCurrentSelection = savedStateHandle.get(CURRENT_SELECTION) ?: 0
        mLimit = savedStateHandle.get(LIMIT) ?: 0
        mCameraCisabled = savedStateHandle.get(DISABLE_CAMERA) ?: false
    }

}