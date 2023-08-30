package com.example.takupdf

import com.example.takupdf.adapters.AdapterPdf
import com.example.takupdf.models.ModelPdf

interface RvListenerPdf {
    //interface methods, we can handle PDF document lick and more options button click in fragment

    fun onPdfClick(modelPdf: ModelPdf, position:Int)
    fun onPdfMoreClick(modelPdf: ModelPdf, position: Int, holder: AdapterPdf.HolderPdf)

}