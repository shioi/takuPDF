package com.example.takupdf.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.takupdf.activities.ImageViewActivity
import com.example.takupdf.R
import com.example.takupdf.models.ModelImage

class AdapterImage(
    private val context: Context,
    private val imageArrayList: ArrayList<ModelImage>
) : RecyclerView.Adapter<AdapterImage.HolderImage>() {

    inner class HolderImage(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageIv = itemView.findViewById<ImageView>(R.id.imageIv)
        var checkBox = itemView.findViewById<CheckBox>(R.id.checkBox)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImage {
        val view = LayoutInflater.from(context).inflate(R.layout.row_image, parent,false)

        return HolderImage(view)
    }

    override fun getItemCount(): Int {
        return imageArrayList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: HolderImage, position: Int) {
        val modelImage = imageArrayList[position]
        val imageUri = modelImage.imageUri

        Glide.with(context)
            .load(imageUri)
            .placeholder(R.drawable.ic_image_add)
            .into(holder.imageIv)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ImageViewActivity::class.java)
            intent.putExtra("imageUri","$imageUri")
            context.startActivity(intent)
        }

        holder.checkBox.setOnCheckedChangeListener{view, isChecked ->
            modelImage.checked = isChecked
        }
    }
}