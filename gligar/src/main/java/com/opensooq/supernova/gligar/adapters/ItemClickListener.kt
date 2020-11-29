package com.opensooq.supernova.gligar.adapters

import com.opensooq.OpenSooq.ui.imagePicker.model.ImageSource

/**
 * Created by Hani AlMomani on 28,November,2019
 */

internal interface ItemClickListener {
    fun onItemClicked(position : Int, source: ImageSource)
}