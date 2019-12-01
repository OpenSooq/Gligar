package com.opensooq.supernova.gligar.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.opensooq.OpenSooq.ui.imagePicker.model.AlbumItem
import com.opensooq.supernova.gligar.R

/**
 * Created by Hani AlMomani on 24,Sep,2019
 */

internal class AlbumsAdapter(var albumItems: List<AlbumItem>, context: Context) : ArrayAdapter<AlbumItem>(context, android.R.layout.simple_list_item_1, albumItems) {
    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getDropDownView(position: Int, view: View?, parent: ViewGroup): View {
        var convertView = view
        val viewHolder: ViewHolderDrop
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_spinner_gligar, parent, false)
            viewHolder = ViewHolderDrop(convertView)
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolderDrop
        }
        val item = getItem(position)
        if (item != null) {
            viewHolder.name?.text = item.name
        }
        return convertView!!
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var convertView = view
        val viewHolder: ViewHolderView
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.layout_album_spinner_gligar, parent, false)
            viewHolder = ViewHolderView(convertView)
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolderView
        }
        val item = getItem(position)
        if (item != null) {
            viewHolder.tvLabel?.text = item.name
        }
        return convertView!!
    }


    internal class ViewHolderView(view: View) {
        var tvLabel: TextView? = null

        init {
            tvLabel = view.findViewById(R.id.tvLabel)
        }
    }

    internal class ViewHolderDrop(view: View) {
        var name: TextView? = null

        init {
            name = view.findViewById(R.id.label)
        }
    }
}