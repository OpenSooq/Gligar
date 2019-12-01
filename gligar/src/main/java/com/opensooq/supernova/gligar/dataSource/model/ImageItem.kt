package com.opensooq.OpenSooq.ui.imagePicker.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Hani AlMomani on 23,April,2019
 */

@Parcelize
internal data class ImageItem(var imagePath: String, var source: ImageSource, var selected: Int) : Parcelable
