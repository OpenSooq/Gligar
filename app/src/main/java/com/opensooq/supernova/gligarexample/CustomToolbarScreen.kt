package com.opensooq.supernova.gligarexample

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.opensooq.supernova.gligar.GligarPicker
import com.opensooq.supernova.gligar.ui.GligarPickerFragment
import com.opensooq.supernova.gligar.ui.GligarPickerListener
import kotlinx.android.synthetic.main.activity_main_custom.*

class CustomToolbarScreen : AppCompatActivity(), GligarPickerListener {

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_custom)
        custom_toolbar?.let {
            setSupportActionBar(it)
            supportActionBar?.setDisplayShowTitleEnabled(true)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDefaultDisplayHomeAsUpEnabled(true)
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, GligarPickerFragment.getInstance(getGligarPicker()))
            .commitNowAllowingStateLoss()
    }

    private fun getGligarPicker(): GligarPicker {
        return GligarPicker()
            .cameraDirect(false)
            .disableCamera(true)
            .limit(20)
            .setCustomBackgroundColor("#FFA07A")
            .supportExtensions(arrayListOf("png", "jpg", "jpeg"))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    override fun onImagesSelected(items: Array<String>) {
        Toast.makeText(this, "Selected Images : ${items.size}", Toast.LENGTH_SHORT).show()
    }

}
