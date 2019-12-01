package com.opensooq.supernova.gligar.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.opensooq.OpenSooq.ui.imagePicker.model.ImageItem
import com.opensooq.OpenSooq.ui.imagePicker.model.ImageSource
import com.opensooq.supernova.gligar.R


/**
 * Created by Hani AlMomani on 24,April,2019
 */

internal class ImagesAdapter(var clickListener: ItemClickListener) :
    RecyclerView.Adapter<ImagesAdapter.ImageViewHolder>() {
    var images = arrayListOf<ImageItem>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder =
        ImageViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_picker_image,
                parent,
                false
            )
        )

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.itemView.setOnClickListener { clickListener.onItemClicked(position) }
        val data = images[position]

        if (data.source== ImageSource.GALLERY) {
            holder.imgView.visibility = View.VISIBLE
            holder.captureView.visibility = View.GONE
            if (data.selected > 0) {
                holder.selectionNum.text = data.selected.toString()
                holder.selectionViews.visibility = ConstraintLayout.VISIBLE
            } else {
                holder.selectionViews.visibility = ConstraintLayout.GONE
                holder.selectionNum.text = data.selected.toString()
            }
            if (data.source == ImageSource.DUM) {
                holder.imgView.visibility = View.VISIBLE
                holder.captureView.visibility = View.GONE
                return
            }
            Glide.with(holder.img).load(data.imagePath)
                .transition(DrawableTransitionOptions().crossFade()).into(holder.img)
            return
        }
        if (data.source== ImageSource.CAMERA) {
            holder.imgView.visibility = View.GONE
            holder.captureView.visibility = View.VISIBLE
        }
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val selectionNum: TextView = itemView.findViewById(R.id.tv_num)
        val selectionViews: View = itemView.findViewById(R.id.v_group)
        val imgView: View = itemView.findViewById(R.id.image_view)
        val captureView: View = itemView.findViewById(R.id.capture_view)
        val img: ImageView = itemView.findViewById(R.id.img_image)
    }
}