package com.opensooq.supernova.gligar.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.FragmentActivity
import com.opensooq.supernova.gligar.GligarPicker

abstract class GligarScreenResult : ActivityResultContract<Any, Array<String>?>() {

    companion object {
        const val ACTION = "com.opensooq.supernova.gligar.action.GLIGAR_SCREEN_ACTION"
        val PICKER_REQUEST_CODE = 30

    }

    override fun createIntent(context: Context, input: Any?): Intent {
        return getGligarIntentLauncher()
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Array<String>? {
        return when (resultCode) {
            Activity.RESULT_OK -> intent?.extras?.getStringArray(GligarPicker.IMAGES_RESULT)
            else -> null
        }
    }

    abstract fun getGligarIntentLauncher(): Intent

}
