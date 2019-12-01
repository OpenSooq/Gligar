package com.opensooq.supernova.gligar.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.opensooq.OpenSooq.ui.imagePicker.model.AlbumItem
import com.opensooq.OpenSooq.ui.imagePicker.model.ImageItem
import com.opensooq.supernova.gligar.GligarPicker.Companion.IMAGES_RESULT
import com.opensooq.supernova.gligar.R
import com.opensooq.supernova.gligar.adapters.AlbumsAdapter
import com.opensooq.supernova.gligar.adapters.ImagesAdapter
import com.opensooq.supernova.gligar.adapters.ItemClickListener
import com.opensooq.supernova.gligar.adapters.LoadMoreListener
import com.opensooq.supernova.gligar.utils.PAGE_SIZE
import com.opensooq.supernova.gligar.utils.createTempImageFile
import java.io.File


/**
 * Created by Hani AlMomani on 24,September,2019
 */


internal class ImagePickerActivity : AppCompatActivity(), LoadMoreListener.OnLoadMoreListener,
    ItemClickListener {

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    companion object {
        const val EXTRA_LIMIT = "limit"
        const val EXTRA_CAMERA_DIRECT = "camera_direct"
        const val EXTRA_DISABLE_CAMERA = "disable_camera"
        const val STORAGE_PERMISSION_REQUEST_CODE = 100
        const val CAMERA_PERMISSION_REQUEST_CODE = 101
        private const val REQUEST_CODE_CAMERA_IMAGE = 102

        fun startActivityForResult(
            fragment: Fragment,
            requestCode: Int,
            intent: Intent
        ) {
            intent.setClass(fragment.context!!, ImagePickerActivity::class.java)
            fragment.startActivityForResult(intent, requestCode)
        }

        fun startActivityForResult(
            activity: Activity,
            requestCode: Int,
            intent: Intent
        ) {
            intent.setClass(activity, ImagePickerActivity::class.java)
            activity.startActivityForResult(intent, requestCode)
        }
    }

    private lateinit var mainViewModel: PickerViewModel
    private var mAlbumAdapter: AlbumsAdapter? = null
    private var mImagesAdapter: ImagesAdapter? = null
    private var loadMoreListener: LoadMoreListener? = null
    private var isPermissionGranted = false
    private var isSaveState = false
    private var forceCamera = false

    private lateinit var icDone: ImageView
    private lateinit var alertBtn: MaterialButton
    private lateinit var alert: View
    private lateinit var albumsSpinner: AppCompatSpinner
    private lateinit var rvImages: RecyclerView
    private lateinit var changeAlbum: View
    private lateinit var rootView: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_picker_gligar)

        icDone = findViewById(R.id._ic_done)
        alertBtn = findViewById(R.id._alert_btn)
        alert = findViewById(R.id._v_alert)
        albumsSpinner = findViewById(R.id._albums_spinner)
        rvImages = findViewById(R.id._rv_images)
        changeAlbum = findViewById(R.id._change_album)
        rootView = findViewById(R.id._v_rootView)


        mainViewModel = ViewModelProvider(this, SavedStateViewModelFactory(application, this)).get(
            PickerViewModel::class.java
        )
        mainViewModel.init(contentResolver)
        if (savedInstanceState != null) {
            isSaveState = true
            mainViewModel.loadSaveState()
        } else {
            mainViewModel.bindArguments(intent.extras)

        }
        setImagesAdapter()
        icDone.setOnClickListener { sendResults() }
    }

    private fun openCamera() = checkCameraPermission()

    private fun loadAlbums() {
        isPermissionGranted = true
        mainViewModel.loadAlbums()
    }

    private fun storagePermissionGranted() {
        hideAlert()
        loadAlbums()
    }

    private fun cameraPermissionGranted() {
        hideAlert()
        try {
            val photoFile: File? = createTempImageFile(this)
            mainViewModel.mCurrentPhotoPath = photoFile?.absolutePath
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val myPhotoFileUri = FileProvider.getUriForFile(
                this,
                this.applicationContext.packageName + ".provider",
                photoFile!!
            )
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, myPhotoFileUri)
            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(
                Intent.createChooser(cameraIntent, ""),
                REQUEST_CODE_CAMERA_IMAGE
            )
        } catch (e: Exception) {
            Log.e("Picker", e.message, e)
        }
    }

    private fun checkCameraPermission() {
        if (mainViewModel.isOverLimit()) {
            showLimitMsg()
            return
        }

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission(android.Manifest.permission.CAMERA)) {
                cameraPermissionGranted()
            } else {
                requestPermission(
                    arrayOf(android.Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST_CODE
                )
            }
        } else {
            cameraPermissionGranted()
        }
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                storagePermissionGranted()
            } else {
                requestPermission(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_REQUEST_CODE
                );
            }
        } else {
            storagePermissionGranted()
        }
    }

    private fun checkPermission(permission: String): Boolean {
        val result = ContextCompat.checkSelfPermission(this, permission)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(permissions: Array<String>, requestCode: Int) {
        if (shouldShowRequestPermissionRationale(this, permissions[0])) {
            showAlert()
        } else {
            requestPermissions(this, permissions, requestCode);
        }
    }

    private fun showAlert() {
        alertBtn.setOnClickListener { showAppPage() }
        alert.visibility = View.VISIBLE
    }

    private fun hideAlert() {
        alert.visibility = View.GONE
    }

    private fun setAlbumsAdapter(albums: List<AlbumItem>) {
        mAlbumAdapter = AlbumsAdapter(albums, applicationContext)
        albumsSpinner.adapter = mAlbumAdapter
        albumsSpinner.setSelection(mainViewModel.mCurrentSelectedAlbum, false)
        albumsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                adapterView: AdapterView<*>,
                view: View,
                pos: Int,
                l: Long
            ) {
                mainViewModel.onAlbumChanged(mAlbumAdapter?.getItem(pos), pos)
                mImagesAdapter?.images?.clear()
                mImagesAdapter?.notifyDataSetChanged()
            }
        }
        changeAlbum.setOnClickListener { albumsSpinner.performClick() }
    }

    private fun setImagesAdapter() {
        mImagesAdapter = ImagesAdapter(this)
        val mLayoutManager = GridLayoutManager(this, 3)
        rvImages.layoutManager = mLayoutManager
        rvImages.setHasFixedSize(true)
        mImagesAdapter?.images = arrayListOf()
        mImagesAdapter?.images?.addAll(if (isSaveState && !mainViewModel.saveStateImages.isNullOrEmpty()) mainViewModel.saveStateImages else mainViewModel.dumpImagesList)
        mainViewModel.saveStateImages.clear()
        rvImages.adapter = mImagesAdapter
        observe()
        setLoadMoreListener()
    }

    override fun onItemClicked(position: Int) {
        when (position) {
            0 -> openCamera()
            else -> {
                mainViewModel.setImageSelection(position, mImagesAdapter?.images)
            }
        }
    }

    private fun observe() {

        mainViewModel.mDirectCamera.observe(this, Observer {
            forceCamera = it
        })
        mainViewModel.mAlbums.observe(this, Observer {
            setAlbumsAdapter(it)
        })
        mainViewModel.mLastAddedImages.observe(this, Observer {
            addImages(it)
        })
        mainViewModel.mNotifyPosition.observe(this, Observer {
            mImagesAdapter?.notifyItemChanged(it)
        })

        mainViewModel.mNotifyInsert.observe(this, Observer {
            mImagesAdapter?.notifyDataSetChanged()
        })

        mainViewModel.mDoneEnabled.observe(this, Observer {
            setDoneVisibilty(it)
        })

        mainViewModel.showOverLimit.observe(this, Observer {
            showLimitMsg()
        })
    }

    private fun addImages(it: ArrayList<ImageItem>) {
        loadMoreListener?.setFinished(false)
        if (it.isNullOrEmpty()) {
            loadMoreListener?.setFinished()
            loadMoreListener?.setLoaded()
            return
        }
        val isFirstPage = mainViewModel.mPage == 0
        isPermissionGranted = true
        if (it.size < PAGE_SIZE) {
            loadMoreListener?.setFinished()
        }

        var lastPos = mImagesAdapter?.images?.size ?: 0
        if (isFirstPage) {
            mImagesAdapter?.images = it
            mImagesAdapter?.notifyDataSetChanged()
        } else {
            mImagesAdapter?.images?.addAll(it)
            mImagesAdapter?.notifyItemRangeInserted(lastPos, it.size)
        }
        loadMoreListener?.setLoaded()
        if (forceCamera) {
            openCamera()
            forceCamera = false
        }
    }

    private fun setDoneVisibilty(visible: Boolean) {
        icDone.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun setLoadMoreListener() {
        if (loadMoreListener != null) {
            rvImages.removeOnScrollListener(loadMoreListener!!)
        }
        loadMoreListener = LoadMoreListener(rvImages.layoutManager as GridLayoutManager)
        loadMoreListener?.setOnLoadMoreListener(this@ImagePickerActivity)
        rvImages.addOnScrollListener(loadMoreListener!!)
    }

    override fun onLoadMore() {
        if (!isPermissionGranted) {
            return
        }
        mainViewModel.loadMoreImages()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_CAMERA_IMAGE -> {
                    mainViewModel.addCameraItem(mImagesAdapter?.images)
                    setDoneVisibilty(true)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            STORAGE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    storagePermissionGranted()
                } else {
                    showAlert()
                }
                return
            }
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraPermissionGranted()
                } else {
                    showAlert()
                }
                return
            }
        }
    }

    private fun sendResults() {
        val images = mainViewModel.getSelectedPaths()
        val intent = Intent()
        intent.putExtra(IMAGES_RESULT, images)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun showLimitMsg() {
        Snackbar.make(rootView, R.string.over_limit_msg, Snackbar.LENGTH_LONG).show()
    }


    private fun showAppPage() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        checkStoragePermission()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (::mainViewModel.isInitialized) {
            mainViewModel.saveStateImages = mImagesAdapter?.images ?: arrayListOf()
            mainViewModel.saveState()
        }
        super.onSaveInstanceState(outState)
    }

}
