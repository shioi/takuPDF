package com.example.takupdf.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.takupdf.Methods
import java.util.concurrent.Executors
import com.example.takupdf.R
import com.example.takupdf.RvListenerPdf
import com.example.takupdf.models.ModelPdf
import java.lang.Exception

class AdapterPdf(
    private val context: Context,
    private val pdfArrayList: ArrayList<ModelPdf>,
    private val rvListenerPdf: RvListenerPdf
): RecyclerView.Adapter<AdapterPdf.HolderPdf>(){


    companion object {
        private const val TAG = "ADAPTER_PDF_TAG"
    }

    inner class HolderPdf(itemView: View): RecyclerView.ViewHolder(itemView) {

        var thumbnailIv: ImageView = itemView.findViewById(R.id.thumbnailIv)
        var nameTv: TextView = itemView.findViewById(R.id.nameTv)
        var pagesTv: TextView = itemView.findViewById(R.id.pageTv)
        var sizeTv: TextView = itemView.findViewById(R.id.sizeTv)
        var moreBtn: ImageButton = itemView.findViewById(R.id.moreBtn)
        var dateTv: TextView = itemView.findViewById(R.id.dateTv)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdf {
        //init/inflate the recylerview item
        val view = LayoutInflater.from(context).inflate(R.layout.row_pdf,parent,false)
        return HolderPdf(view)
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun onBindViewHolder(holder: HolderPdf, position: Int) {
        val modelPdf = pdfArrayList[position]
        val name = modelPdf.file.name

        val timestamp = modelPdf.file.lastModified()

        val formattedDate: String = Methods.formatTimestamp(timestamp)

        loadThumbnailFromPdf(modelPdf, holder)
        loadFileSize(modelPdf, holder)

        holder.nameTv.text = name
        holder.dateTv.text = formattedDate

        holder.itemView.setOnClickListener {
            rvListenerPdf.onPdfClick(modelPdf, position)
        }

        holder.moreBtn.setOnClickListener {
            rvListenerPdf.onPdfMoreClick(modelPdf, position, holder)
        }
    }

    private fun loadFileSize(modelPdf: ModelPdf, holder: AdapterPdf.HolderPdf) {
        Log.d(TAG, "loadFileSize: ")

        val bytes: Double = modelPdf.file.length().toDouble()
        val kb = bytes / 1024

        val mb = kb / 1024
        var size = ""
        size = if(mb >= 1) {
            String.format("%.2f",mb) + " MB"
        } else if (kb >= 1){
            String.format("%.2f",kb) + " KB"
        } else {
            String.format("%.2f",bytes) + " Bytes"
        }

        Log.d(TAG, "loadFileSize: Size: $size")

        //set file size to sizeTv
        holder.sizeTv.text = size
    }

    private fun loadThumbnailFromPdf(modelPdf: ModelPdf, holder: AdapterPdf.HolderPdf) {
        Log.d(TAG, "loadThumbnailFromPdf: ")
        val executorService = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())

        executorService.execute {
            var thumbnailBitmap: Bitmap?= null
            var pageCount = 0

            try {

                val parcelFileDescriptor = ParcelFileDescriptor.open(modelPdf.file,ParcelFileDescriptor.MODE_READ_ONLY)
                val pdfRenderer = PdfRenderer(parcelFileDescriptor)

                pageCount = pdfRenderer.pageCount
                if(pageCount <= 0) {
                    Log.d(TAG, "loadThumbnailFromPdf: No Pages")
                } else {
                    val currentPage = pdfRenderer.openPage(0)
                    thumbnailBitmap = Bitmap.createBitmap(currentPage.width, currentPage.height,Bitmap.Config.ARGB_8888)
                    currentPage.render(thumbnailBitmap,null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                }
            } catch (e: Exception) {
                Log.e(TAG, "loadThumbnailFromPdf: ",e )
            }

            handler.post {
                Log.d(TAG, "loadThumbnailFromPdf: Setting thumnbail and page count")
                Glide.with(context)
                    .load(thumbnailBitmap)
                    .fitCenter()
                    .placeholder(R.drawable.ic_pdf_gray)
                    .into(holder.thumbnailIv)
                
                holder.pagesTv.text = "$pageCount Pages"
            }

        }
    }
}
