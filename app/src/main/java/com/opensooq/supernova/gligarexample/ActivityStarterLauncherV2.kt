package com.opensooq.supernova.gligarexample

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.opensooq.supernova.gligar.ui.GligarResultBuilder
import kotlinx.android.synthetic.main.activity_main.*

class ActivityStarterLauncherV2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val starter = registerForActivityResult(object : GligarResultBuilder(this@ActivityStarterLauncherV2) {
            override fun build(activity: FragmentActivity?, fragment: Fragment?, input: List<String>?): Intent {
                this addLimitImages 20
                this addRequestCode REQUEST_CODE
                this disableCamera true
                this isCustomExtSuported arrayListOf("png", "jpg", "jpeg")
                this cameraDirect true
                this isSingleSelection false
                this isPreItemsSelected input
                return getFinalIntent()
            }
        }) {
            if (!it.isNullOrEmpty()) {
                imagesCount.text = "Number of selected Images: ${it.size}"
            }
        }

        starter.launch(arrayListOf(
            "/storage/emulated/0/DCIM/Screenshots/Screenshot_20201129-101300_Facebook.jpg",
            "/storage/emulated/0/DCIM/Screenshots/Screenshot_20201129-093307_Chrome.jpg",
            "/storage/emulated/0/DCIM/Screenshots/Screenshot_20201129-071900_Facebook.jpg",
            "/storage/emulated/0/DCIM/Screenshots/Screenshot_20201129-071500_Facebook.jpg"
        ))

    }
}