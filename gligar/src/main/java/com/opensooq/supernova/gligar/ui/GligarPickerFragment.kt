package com.opensooq.supernova.gligar.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.opensooq.OpenSooq.ui.imagePicker.model.AlbumItem
import com.opensooq.OpenSooq.ui.imagePicker.model.ImageItem
import com.opensooq.OpenSooq.ui.imagePicker.model.ImageSource
import com.opensooq.supernova.gligar.GligarPicker
import com.opensooq.supernova.gligar.R
import com.opensooq.supernova.gligar.adapters.AlbumsAdapter
import com.opensooq.supernova.gligar.adapters.ImagesAdapter
import com.opensooq.supernova.gligar.adapters.ItemClickListener
import com.opensooq.supernova.gligar.adapters.LoadMoreListener
import com.opensooq.supernova.gligar.utils.PAGE_SIZE
import com.opensooq.supernova.gligar.utils.createTempImageFile
import kotlinx.android.synthetic.main.activity_image_picker_gligar.*
import kotlinx.android.synthetic.main.include_permssion_alert_gligar.*
import java.io.File
import java.lang.IllegalArgumentException

class GligarPickerFragment : Fragment(), LoadMoreListener.OnLoadMoreListener, ItemClickListener {


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
    private var listener: GligarPickerListener? = null

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    companion object {
        fun getInstance(picker: GligarPicker): GligarPickerFragment {
            val fragment = GligarPickerFragment()
            fragment.arguments = picker.getBuildBundle()
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_image_picker_gligar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            mainViewModel = ViewModelProvider(it, SavedStateViewModelFactory(it.application, this)).get(
                PickerViewModel::class.java
            )

            initFragmentOperations(savedInstanceState, it)
        }
    }

    private fun initFragmentOperations(savedInstanceState: Bundle?, activity: FragmentActivity) {
        icDone = _ic_done
        alertBtn = _alert_btn
        alert = _v_alert
        albumsSpinner = _albums_spinner
        rvImages = _rv_images
        changeAlbum = _change_album
        rootView = _v_rootView

        mainViewModel.init(activity.contentResolver)
        if (savedInstanceState != null) {
            isSaveState = true
            mainViewModel.loadSaveState()
        } else {
            arguments?.let {
                it.getString(ImagePickerActivity.EXTRA_CUSTOM_COLOR, "")?.let {
                   if (!TextUtils.isEmpty(it)) {
                       try {
                           pickerToolbar?.setBackgroundColor(Color.parseColor(it))
                       } catch (ex: Exception) {
                           println(ex.message)
                           ex.printStackTrace()
                       }
                   }
                }
                mainViewModel.bindArguments(it)
            }
        }
        setImagesAdapter()
        icDone.setOnClickListener { sendResults() }
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
                    ImagePickerActivity.CAMERA_PERMISSION_REQUEST_CODE
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
                    ImagePickerActivity.STORAGE_PERMISSION_REQUEST_CODE
                );
            }
        } else {
            storagePermissionGranted()
        }
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
            val photoFile: File? = createTempImageFile(requireActivity())
            mainViewModel.mCurrentPhotoPath = photoFile?.absolutePath
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val myPhotoFileUri = activity?.let {
                FileProvider.getUriForFile(it,
                    activity?.applicationContext?.packageName + ".provider",
                    photoFile!!
                )
            }
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, myPhotoFileUri)
            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(
                Intent.createChooser(cameraIntent, ""),
                ImagePickerActivity.REQUEST_CODE_CAMERA_IMAGE
            )
        } catch (e: Exception) {
            Log.e("Picker", e.message, e)
        }
    }

    private fun checkPermission(permission: String): Boolean {
        val result = ContextCompat.checkSelfPermission(requireActivity(), permission)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(permissions: Array<String>, requestCode: Int) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permissions[0])) {
            showAlert()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), permissions, requestCode);
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as GligarPickerListener
        } catch (ex: Exception) {
            throw IllegalArgumentException("Your Activity Should Implement GligarPickerListener")
        }
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    private fun showAlert() {
        alertBtn.setOnClickListener { showAppPage() }
        alert.visibility = View.VISIBLE
    }

    private fun hideAlert() {
        alert.visibility = View.GONE
    }

    private fun setAlbumsAdapter(albums: List<AlbumItem>) {
        mAlbumAdapter = AlbumsAdapter(albums, requireActivity().applicationContext)
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
        val mLayoutManager = GridLayoutManager(requireActivity(), 3)
        rvImages.layoutManager = mLayoutManager
        rvImages.setHasFixedSize(true)
        mImagesAdapter?.images = arrayListOf()
        mImagesAdapter?.images?.addAll(if (isSaveState && !mainViewModel.saveStateImages.isNullOrEmpty()) mainViewModel.saveStateImages else mainViewModel.dumpImagesList)
        mainViewModel.saveStateImages.clear()
        rvImages.adapter = mImagesAdapter
        observe()
        setLoadMoreListener()
    }

    override fun onItemClicked(position: Int, source: ImageSource) {
        if (source == ImageSource.CAMERA) {
            openCamera()
        } else {
            mainViewModel.setImageSelection(position, mImagesAdapter?.images)
        }
    }

    private fun observe() {

        mainViewModel.mDirectCamera.observe(viewLifecycleOwner, Observer {
            forceCamera = it
        })
        mainViewModel.mAlbums.observe(viewLifecycleOwner, Observer {
            setAlbumsAdapter(it)
        })
        mainViewModel.mLastAddedImages.observe(viewLifecycleOwner, Observer {
            addImages(it)
        })
        mainViewModel.mNotifyPosition.observe(viewLifecycleOwner, Observer {
            mImagesAdapter?.notifyItemChanged(it)
        })

        mainViewModel.mNotifyInsert.observe(viewLifecycleOwner, Observer {
            mImagesAdapter?.notifyDataSetChanged()
        })

        mainViewModel.mDoneEnabled.observe(viewLifecycleOwner, Observer {
            setDoneVisibilty(it)
        })

        mainViewModel.showOverLimit.observe(viewLifecycleOwner, Observer {
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
        loadMoreListener?.setOnLoadMoreListener(this@GligarPickerFragment)
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
                ImagePickerActivity.REQUEST_CODE_CAMERA_IMAGE -> {
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
            ImagePickerActivity.STORAGE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    storagePermissionGranted()
                } else {
                    showAlert()
                }
                return
            }
            ImagePickerActivity.CAMERA_PERMISSION_REQUEST_CODE -> {
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
        listener?.onImagesSelected(mainViewModel.getSelectedPaths())
        activity?.onBackPressed()
    }

    private fun showLimitMsg() {
        Snackbar.make(rootView, R.string.over_limit_msg, Snackbar.LENGTH_LONG).show()
    }


    private fun showAppPage() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", requireActivity().packageName, null)
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
