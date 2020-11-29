package com.opensooq.supernova.gligar.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.opensooq.supernova.gligar.GligarPicker
import com.opensooq.supernova.gligar.utils.getSupportedImagesExt

abstract class GligarResultBuilder constructor(
    private val activity: FragmentActivity? = null,
    private val fragment: Fragment? = null
) : ActivityResultContract<List<String>?, Array<String>?>() {

    private var limit: Int = 10
    private var disableCamera: Boolean = false
    private var cameraDirect: Boolean = false
    private var isSingleSelection: Boolean = false
    private var supportedExt: ArrayList<String> = arrayListOf()
    private var isPreSelectedItemsAttached: Boolean = false
    private var requestCode: Int = REQUEST_CODE
    private var items: List<String>? = null
    companion object {
        const val REQUEST_CODE = 234
    }

    override fun createIntent(context: Context, input: List<String>?): Intent {
        if (activity == null && fragment == null) {
            throw IllegalArgumentException("Gligar Should has Activity or Fragment to Start")
        }
        return build(activity, fragment, input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Array<String>? {
        return when (resultCode) {
            Activity.RESULT_OK -> intent?.extras?.getStringArray(GligarPicker.IMAGES_RESULT)
            else -> null
        }
    }

    fun getFinalIntent(): Intent {
        val intent = Intent()
        intent.putExtra(ImagePickerActivity.EXTRA_LIMIT, limit)
        intent.putExtra(ImagePickerActivity.EXTRA_CAMERA_DIRECT, cameraDirect)
        intent.putExtra(ImagePickerActivity.EXTRA_SINGLE_SELECTION, isSingleSelection)
        intent.putExtra(ImagePickerActivity.EXTRA_SUPPRTED_TYPES, getSupportedImagesExt(supportedExt))
        if (!cameraDirect) {
            intent.putExtra(ImagePickerActivity.EXTRA_DISABLE_CAMERA, disableCamera)
        }

        if (isPreSelectedItemsAttached) {
            items?.let {
                intent.putExtra(ImagePickerActivity.EXTRA_PRE_SELECTED, isPreSelectedItemsAttached)
                intent.putExtra(ImagePickerActivity.EXTRA_PRE_SELECTED_ITEMS, it.toTypedArray())
            }
        }

        if (activity != null) {
            ImagePickerActivity.startActivityForResult(activity, requestCode, intent)
        } else {
            fragment?.let {
                ImagePickerActivity.startActivityForResult(it, requestCode, intent)
            }
        }

        return intent
    }

    infix fun addRequestCode(requestCode: Int) {
        this.requestCode = requestCode
    }

    infix fun isPreItemsSelected(items: List<String>?) {
        this.isPreSelectedItemsAttached = true
        this.items = items
    }

    infix fun isCustomExtSuported(supportedExt: ArrayList<String>) {
        this.supportedExt = supportedExt
    }

    infix fun isSingleSelection(isSingleSelection: Boolean) {
        this.isSingleSelection = isSingleSelection
    }

    infix fun cameraDirect(cameraDirect: Boolean) {
        this.cameraDirect = cameraDirect
    }

    infix fun disableCamera(disableCamera: Boolean) {
        this.disableCamera = disableCamera
    }

    infix fun addLimitImages(limit: Int) {
        this.limit = limit
    }

    abstract fun build(activity: FragmentActivity?, fragment: Fragment?, input: List<String>?): Intent

}
