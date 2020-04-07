package com.opensooq.OpenSooq.ui.imagePicker.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Hani AlMomani on 24,April,2019
 */
@Parcelize
internal data class AlbumItem(val name: String, val isAll: Boolean,val bucketId: String) : Parcelable