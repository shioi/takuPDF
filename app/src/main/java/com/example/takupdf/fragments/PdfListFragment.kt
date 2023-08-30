package com.example.takupdf.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.takupdf.Constants
import com.example.takupdf.R
import com.example.takupdf.RvListenerPdf
import com.example.takupdf.activities.PdfViewActivity
import com.example.takupdf.adapters.AdapterPdf
import com.example.takupdf.models.ModelPdf
import java.io.File

class PdfListFragment : Fragment() {
    //recycle view
    private lateinit var pdfRv: RecyclerView
    private lateinit var pdfArrayList: ArrayList<ModelPdf>
    private lateinit var adapterPdf: AdapterPdf
    private lateinit var mContext: Context

    companion object
    {
        private const val TAG = "PDF_LIST_TAG"
    }

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pdf_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //init ui views
        pdfRv = view.findViewById(R.id.pdfRv)

        loadPdfDocuments()
    }

    private fun loadPdfDocuments() {
        Log.d(TAG, "loadPdfDocuments: ")

        pdfArrayList = ArrayList()
        adapterPdf = AdapterPdf(mContext, pdfArrayList, object: RvListenerPdf {
            override fun onPdfClick(modelPdf: ModelPdf, position: Int) {

                val intent = Intent(mContext, PdfViewActivity::class.java)
                intent.putExtra("pdfUri", "${modelPdf.uri}")
                startActivity(intent)
            }

            override fun onPdfMoreClick(
                modelPdf: ModelPdf,
                position: Int,
                holder: AdapterPdf.HolderPdf
            ) {
                showMoreOptionsDialog(modelPdf, holder)
            }
        })


        pdfRv.adapter = adapterPdf
        pdfRv.layoutManager = LinearLayoutManager(mContext)

        val folder = File(mContext.getExternalFilesDir(null), Constants.PDF_FOLDER)

        if(folder.exists()) {
            val files = folder.listFiles()
            Log.d(TAG, "loadPdfDocuments: FilesCount ${files!!.size}")
            
            for (fileEntry in files) {
                Log.d(TAG, "loadPdfDocuments: FileName: ${fileEntry.name}")
                val uri = Uri.fromFile(fileEntry)
                val modelPdf = ModelPdf(fileEntry, uri)

                pdfArrayList.add(modelPdf)
                adapterPdf.notifyItemInserted(pdfArrayList.size)
            }
        } else {
            Log.d(TAG, "loadPdfDocuments: No Pdf Files yet" )
        }
    }

    private fun showMoreOptionsDialog(modelPdf: ModelPdf, holder: AdapterPdf.HolderPdf) {
        Log.d(TAG, "showMoreOptionsDialog: ")

        val popupMenu = PopupMenu(mContext, holder.moreBtn)
        popupMenu.menu.add(Menu.NONE, 0,0,"Rename")
        popupMenu.menu.add(Menu.NONE, 1,1,"Delete")
        popupMenu.menu.add(Menu.NONE, 2,2,"Share")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {
            when(it.itemId) {
                0 -> {
                    //rename is clicked
                    pdfRename(modelPdf)
                }
                1 -> {
                    //delete is clicked
                    pdfDelete(modelPdf)
                }
                2 -> {
                    //share is clicked
                    pdfShare(modelPdf)
                }
            }
            true
        }
    }


    private fun pdfRename(modelPdf: ModelPdf) {
        Log.d(TAG, "pdfRename: ")

        val view = LayoutInflater.from(mContext).inflate(R.layout.dialog_rename, null)

        val pdfNewNameEt = view.findViewById<EditText>(R.id.pdfNewNameEt)
        val renameBtn = view.findViewById<Button>(R.id.renameBtn)

        val prevName = "${modelPdf.file.nameWithoutExtension}"
        Log.d(TAG, "pdfRename: prevName: $prevName")

        pdfNewNameEt.setText(prevName)

        val builder = AlertDialog.Builder(mContext)
        builder.setView(view)

        val alertDialog = builder.create()
        alertDialog.show()

        renameBtn.setOnClickListener {
            //get the name user input in the pdfNewName
            val newName = pdfNewNameEt.text.toString().trim()
            Log.d(TAG, "pdfRename: newName $newName")

            if(newName.isEmpty()){
                Toast.makeText(mContext, "Enter name....!", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    val newFile = File(mContext.getExternalFilesDir(null), "${Constants.PDF_FOLDER}/${newName}.pdf")
                    modelPdf.file.renameTo(newFile)
                    Toast.makeText(mContext, "Renamed Successfully...", Toast.LENGTH_SHORT).show()
                    loadPdfDocuments()
                }catch (e: Exception) {
                    Toast.makeText(mContext, "Failed to rename due to ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "pdfRename: ", e)
                }

                alertDialog.dismiss()
            }
        }

    }

    private fun pdfDelete(modelPdf: ModelPdf) {
        Log.d(TAG, "pdfDelete: ")

        val dialog = AlertDialog.Builder(mContext)
        dialog.setTitle("Delete File")
            .setMessage("Are you sure you want  to delete ${modelPdf.file.name} ?")
            .setPositiveButton("DELETE") {_,_ ->
                try {
                    modelPdf.file.delete()
                    Toast.makeText(mContext, "Deleted successfully...", Toast.LENGTH_SHORT).show()
                    //file is delete so load again
                    loadPdfDocuments()
                } catch (e: Exception) {
                    Log.e(TAG, "pdfDelete: ", e)
                    Toast.makeText(mContext, "Failed to delete due to ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("CANCEL") {dialogs, _ ->
                dialogs.dismiss()
            }
            .show()
    }

    private fun pdfShare(modelPdf: ModelPdf) {
        Log.d(TAG, "pdfShare: ")

        val file = modelPdf.file

        val fileUri = FileProvider.getUriForFile(mContext, "com.example.takupdf.fileprovider",file)

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/pdf"
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        intent.putExtra(Intent.EXTRA_STREAM, fileUri)

        startActivity(Intent.createChooser(intent, "Share PDF"))
    }

}