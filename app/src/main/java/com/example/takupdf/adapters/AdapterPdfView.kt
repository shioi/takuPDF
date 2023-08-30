package com.example.takupdf.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.takupdf.R
import com.example.takupdf.models.ModelPdfView

class AdapterPdfView (
    private val context: Context,
    private val pdfViewArrayList: ArrayList<ModelPdfView>
    ): RecyclerView.Adapter<AdapterPdfView.HolderPdfView>() {

    inner class HolderPdfView(itemView: View): ViewHolder(itemView) {
        val pageNumberTv: TextView = itemView.findViewById(R.id.pageNumberTv)
        val imageTv: ImageView = itemView.findViewById(R.id.imageIV)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterPdfView.HolderPdfView {
        //inflate the recyclerview item
        val view = LayoutInflater.from(context).inflate(R.layout.row_pdf_view, parent, false)

        return HolderPdfView(view)

    }

    override fun onBindViewHolder(holder: HolderPdfView, position: Int) {
        //set data in UI views, handle clicks

        val modelPdfView = pdfViewArrayList[position]
        val pageNumber = position + 1
        val bitmap = modelPdfView.bitmap

        Glide.with(context)
            .load(bitmap)
            .placeholder(R.drawable.icon_add_image_black)
            .into(holder.imageTv)

        holder.pageNumberTv.text = "$pageNumber"

    }

    override fun getItemCount(): Int {
        return pdfViewArrayList.size
    }

}