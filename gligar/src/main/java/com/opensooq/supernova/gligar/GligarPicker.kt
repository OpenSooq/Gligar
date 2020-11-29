package com.opensooq.supernova.gligar

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import com.opensooq.supernova.gligar.ui.GligarResultBuilder
import com.opensooq.supernova.gligar.ui.ImagePickerActivity
import com.opensooq.supernova.gligar.ui.ImagePickerActivity.Companion.EXTRA_CAMERA_DIRECT
import com.opensooq.supernova.gligar.ui.ImagePickerActivity.Companion.EXTRA_DISABLE_CAMERA
import com.opensooq.supernova.gligar.ui.ImagePickerActivity.Companion.EXTRA_LIMIT
import com.opensooq.supernova.gligar.ui.ImagePickerActivity.Companion.EXTRA_SINGLE_SELECTION
import com.opensooq.supernova.gligar.ui.ImagePickerActivity.Companion.EXTRA_SUPPRTED_TYPES
import com.opensooq.supernova.gligar.ui.ImagePickerActivity.Companion.startActivityForResult
import com.opensooq.supernova.gligar.utils.ALL_TYPES
import com.opensooq.supernova.gligar.utils.getSupportedImagesExt
import java.lang.IllegalStateException

/**
 * Created by Hani AlMomani on 30,November,2019
 */


class GligarPicker {

    companion object {
        const val IMAGES_RESULT = "images"
    }

    private var withActivity: Activity? = null
    private var withFragment: Fragment? = null
    private var requestCode: Int = 0
    private var limit: Int = 10
    private var disableCamera: Boolean = false
    private var cameraDirect: Boolean = false
    private var isSingleSelection: Boolean = false
    private var supportedExt: ArrayList<String> = arrayListOf()
    private var isPreSelectedItemsAttached: Boolean = false
    private var items: List<String>? = null


    fun requestCode(requestCode: Int) = apply { this.requestCode = requestCode }
    fun limit(limit: Int) = apply { this.limit = limit }
    fun disableCamera(disableCamera: Boolean) = apply { this.disableCamera = disableCamera }
    fun singleSelection(isSingleSelection: Boolean) = apply { this.isSingleSelection = isSingleSelection }
    fun cameraDirect(cameraDirect: Boolean) = apply { this.cameraDirect = cameraDirect }
    fun withActivity(activity: Activity) = apply { this.withActivity = activity }
    fun withFragment(fragment: Fragment) = apply { this.withFragment = fragment }
    fun supportExtensions(supportedExt: ArrayList<String>) = apply { this.supportedExt = supportedExt }
    fun isPreItemsSelected(items: List<String>?) {
        this.isPreSelectedItemsAttached = true
        this.items = items
    }

    fun show(): Intent {
        if(withActivity == null && withFragment ==null){
            throw IllegalStateException("Activity or fragment should be passed, use withActivity(activity) or withFragment(fragment) to set any.")
        }

        val intent = Intent()
        intent.putExtra(EXTRA_LIMIT, limit)
        intent.putExtra(EXTRA_CAMERA_DIRECT, cameraDirect)
        intent.putExtra(EXTRA_SINGLE_SELECTION, isSingleSelection)
        intent.putExtra(EXTRA_SUPPRTED_TYPES, getSupportedImagesExt(supportedExt))
        if (!cameraDirect) {
            intent.putExtra(EXTRA_DISABLE_CAMERA, disableCamera)
        }

        if (isPreSelectedItemsAttached) {
            items?.let {
                intent.putExtra(ImagePickerActivity.EXTRA_PRE_SELECTED, isPreSelectedItemsAttached)
                intent.putExtra(ImagePickerActivity.EXTRA_PRE_SELECTED_ITEMS, it.toTypedArray())
            }
        }

        if(withActivity!=null){
            startActivityForResult(withActivity!!,requestCode,intent)
        }else{
            startActivityForResult(withFragment!!,requestCode,intent)
        }

        return intent
    }
}