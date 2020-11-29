package com.opensooq.supernova.gligarexample

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main_launcher.*

class MainScreenLauncher : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_launcher)

        normal_launcher_button?.setOnClickListener {
            startActivity(Intent(this@MainScreenLauncher, NormalActivityStarter::class.java))
        }

        start_launcher_button?.setOnClickListener {
            startActivity(Intent(this@MainScreenLauncher, ActivityResultLauncher::class.java))
        }

        supported_files_button?.setOnClickListener {
            startActivity(Intent(this@MainScreenLauncher, SupportedFilesLauncher::class.java))
        }

        pre_selected_files_button?.setOnClickListener {
            startActivity(Intent(this@MainScreenLauncher, ActivityStarterLauncherV2::class.java))
        }

        custom_screen?.setOnClickListener {
            startActivity(Intent(this@MainScreenLauncher, CustomToolbarScreen::class.java))
        }
    }
}