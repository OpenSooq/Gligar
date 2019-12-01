package com.opensooq.supernova.gligarexample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.opensooq.supernova.gligar.Gligar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val PICKER_REQUEST_CODE = 30

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Gligar().limit(10).disableCamera(false).cameraDirect(false).requestCode(PICKER_REQUEST_CODE)
            .withActivity(this).show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            PICKER_REQUEST_CODE -> {
                val imagesList = data?.extras?.getStringArray(Gligar.IMAGES_RESULT)
                if (!imagesList.isNullOrEmpty()) {
                    imagesCount.text = "Number of selected Images: ${imagesList.size}"
                }
            }
        }
    }

}
