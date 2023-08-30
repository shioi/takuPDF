package com.example.takupdf.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.takupdf.Constants
import com.example.takupdf.R
import com.example.takupdf.adapters.AdapterImage
import com.example.takupdf.models.ModelImage
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors
import kotlin.math.log

class ImageFragment : Fragment() {

    companion object {
        private const val TAG = "IMAGE_LIST_TAG"
    }

    private lateinit var mContext: Context

    private lateinit var allImageArrayList: ArrayList<ModelImage>
    private lateinit var adapterImage: AdapterImage
    private lateinit var progressDialog: ProgressDialog
    //uri of image picked
    private var imageUri: Uri? = null

    //UI views
    private lateinit var addImageFab: FloatingActionButton
    private lateinit var imageRv: RecyclerView

    override fun onAttach(context: Context) {
        //in fragment getContext() and getActivity() returns null
        //we need context so it gets passed
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //init UI views
        addImageFab = view.findViewById(R.id.addImageFab)
        imageRv = view.findViewById(R.id.imagesRv)

        progressDialog = ProgressDialog(mContext)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        loadImages()

        addImageFab.setOnClickListener {
            showInputImageDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_images, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.image_item_delete -> {
                val builder = AlertDialog.Builder(mContext)
                builder.setTitle("Delete Image")
                    .setMessage("Are you sure you want to delete All/Selected Images?")
                    .setPositiveButton("DELETE ALL") { _,_ ->
                        deleteImages(true)
                    }
                    .setNeutralButton("DELETE SELECTED") { _,_ ->
                        deleteImages(false)
                    }
                    .setNegativeButton("CANCEL") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()

            }

            R.id.image_item_pdf -> {
                val builder = AlertDialog.Builder(mContext)
                builder.setTitle("Convert to PDF")
                    .setMessage("Convert All/Selected Images to PDF")
                    .setPositiveButton("CONVERT ALL") { _, _ ->
                        convertImageToPdf(true)
                    }
                    .setNeutralButton("CONVERT SELECTED") { _, _ ->
                        convertImageToPdf(false)
                    }
                    .setNegativeButton("CANCEL") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()

            }
        }
        return super.onOptionsItemSelected(item)
    }

    //FOR PDF
    private fun convertImageToPdf(convertAll: Boolean) {
        Log.d(TAG, "convertImagesToPDF: convertAll: $convertAll")

        progressDialog.setMessage("Converting to PDF...")
        progressDialog.show()
        
        //init executorSevice for backgroup processing
        val executorService = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        
        executorService.execute{
            Log.d(TAG, "convertImageToPdf: BG work...")

            var imageToPdfList = ArrayList<ModelImage>()
            if(convertAll){
                imageToPdfList = allImageArrayList
            } else {
                //add all selected
                for (i in allImageArrayList.indices) {
                    if(allImageArrayList[i].checked) {
                        imageToPdfList.add(allImageArrayList[i])
                    }
                }
            }
            Log.d(TAG,"convertImagesToPdf: size: ${imageToPdfList.size}")

            try {
                val root = File(mContext.getExternalFilesDir(null), Constants.PDF_FOLDER)
                root.mkdirs()

                //2) now with extension of the image
                val timestamp = System.currentTimeMillis()
                val filename = "PDF_$timestamp.pdf"

                Log.d(TAG, "convertImageToPdf: fileName $filename")

                val file = File(root, filename)

                val fileOutputStream = FileOutputStream(file)

                val pdfDocument = PdfDocument()
                for(i in imageToPdfList.indices) {
                    val imageToPdfUrl = imageToPdfList[i].imageUri

                    try{
                        var bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            ImageDecoder.decodeBitmap(ImageDecoder.createSource(mContext.contentResolver, imageToPdfUrl))
                        } else {
                            MediaStore.Images.Media.getBitmap(mContext.contentResolver, imageToPdfUrl)
                        }

                        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false)

                        val pageInfo = PageInfo.Builder(bitmap.width, bitmap.height, i+1).create()

                        val page = pdfDocument.startPage(pageInfo)

                        val paint = Paint()
                        paint.color = Color.WHITE

                        //setting up canvas to paint pdf page

                        val canvas = page.canvas
                        canvas.drawPaint(paint)
                        canvas.drawBitmap(bitmap,0f,0f,null)

                        pdfDocument.finishPage(page)

                        bitmap.recycle()

                    } catch (e: Exception) {
                        Log.e(TAG, "convertImageToPdf: ", e)
                    }
                }

                //5) add pdf pages to pdf documewnt
                pdfDocument.writeTo(fileOutputStream)
                pdfDocument.close()

            } catch (e: Exception) {
                Log.e(TAG, "convertImageToPdf: ", e)
            }

            handler.post {
                Log.d(TAG, "convertImageToPdf: Converted...")
                progressDialog.dismiss()
                toast("Converted to PDF")
            }
        }

    }

    private fun deleteImages(deleteAll: Boolean) {
        //init seperate arrayListof images to delete
        var imagesToDeleteList = ArrayList<ModelImage>()
        if(deleteAll){
            imagesToDeleteList = allImageArrayList
        } else {
            for (image in allImageArrayList) {
                if(image.checked){
                    imagesToDeleteList.add(image)
                }
            }
        }

        for(modelImage in imagesToDeleteList){
            try{
                val pathOfImageToDelete = "${modelImage.imageUri.path}"
                val file = File(pathOfImageToDelete)
                if(file.exists()) {
                    val isDeleted = file.delete()
                    Log.d(TAG, "deleteImages: Filed Deleted: $isDeleted")
                }
            } catch (e: Exception) {
                Log.e(TAG, "delete failed :" ,e)
            }
        }

        toast("Deleted")
        loadImages()
    }

    private fun loadImages() {
        Log.d(TAG,"loadImages")

        allImageArrayList = ArrayList()
        adapterImage = AdapterImage(mContext, allImageArrayList)

        imageRv.adapter = adapterImage
        imageRv.layoutManager = GridLayoutManager(mContext,2)

        val folder = File(mContext.getExternalFilesDir(null), Constants.IMAGE_FOLDER)

        if (folder.exists()){
            Log.d(TAG, "loadImages: ")
            val files = folder.listFiles()

            if(files != null) {
                //files exists, lets load them
                Log.d(TAG, "loadImages: Folder have files")
                for(file in files) {
                    Log.d(TAG,"loadImages: fileName: ${file.name}")

                    val imageUri = Uri.fromFile(file)

                    val modeImage = ModelImage(imageUri, false)

                    allImageArrayList.add(modeImage)

                    adapterImage.notifyItemInserted(allImageArrayList.size)


                }
            } else {
                Log.d(TAG, "loadImages: Folder exist but have not files ")
            }

        } else {
            Log.d(TAG, "loadImages: Folder doesn't exist ")
        }

    }

    private fun saveImageTopAppLevelDirectory(imageUriToBeSaved: Uri) {
        Log.d(TAG, "saveimagetoappleveldirectory: $imageUriToBeSaved")

        try {
            val bitmap: Bitmap
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(mContext.contentResolver, imageUriToBeSaved))
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(mContext.contentResolver, imageUriToBeSaved)
            }

            val directory = File(mContext.getExternalFilesDir(null), Constants.IMAGE_FOLDER)
            directory.mkdirs()

            val timestamp = System.currentTimeMillis()
            val filename = "$timestamp.jpeg"

            val file = File(mContext.getExternalFilesDir(null), "${Constants.IMAGE_FOLDER}/$filename")

            //save image

            try {
                val fos = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.flush()
                fos.close()
                Log.d(TAG, "saveImageToAppLevelDirectory: Image saved")
                toast("Image saved")
            } catch(e: java.lang.Exception){
                Log.d(TAG, "saveImageToAppLevelDirectory", e)
                Log.d(TAG, "saveImageToAppLevelDirectory: ${e.message}")
                toast("failed to save image due to ${e.message}")
            }

        } catch (e: Exception){
            Log.d(TAG, "saveImageToAppLevelDirectory", e)
            Log.d(TAG, "saveImageToAppLevelDirectory: failed to prepare image${e.message}")

            toast("failed to prepare image due to ${e.message}")
        }

    }

    private fun showInputImageDialog() {
        Log.d(TAG,"showInputImageDialog:")
        val popupMenu = PopupMenu(mContext, addImageFab)

        popupMenu.menu.add(Menu.NONE, 1, 1, "CAMERA")
        popupMenu.menu.add(Menu.NONE, 2, 2, "GALLERY")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {
            if(it.itemId == 1) {
                //camera is clicked, check if permission granted
                Log.d(TAG,"camera is clicked, check if permission granted")
                if(checkCameraPermission()) {
                    pickImageCamera()
                } else {
                    requestCameraPermission.launch(arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
                }

            } else if (it.itemId == 2){
                Log.d(TAG,"gallery is clicked, check if permission granted")
                if(checkStoragePermission()) {
                    pickImageGallery()
                } else {
                    requestStoragePermission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun pickImageGallery() {
        Log.d(TAG, "pickImageGallery: ")
        val intent = Intent(Intent.ACTION_PICK)

        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if(result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            imageUri = data!!.data
            Log.d(TAG, "galleryActivityResultLauncher: Gallery Image $imageUri")
            saveImageTopAppLevelDirectory(imageUri!!)

            //notrify the adapter that a new image is inserted
            val modelImage = ModelImage(imageUri!!,false)
            allImageArrayList.add(modelImage)
            adapterImage.notifyItemInserted(allImageArrayList.size)
        } else {
            Log.d(TAG, "galleryActivityResultLauncher: Cancelled")
            toast("cancelled")
        }
    }

    private fun pickImageCamera(){
        Log.d(TAG, "pickImageCamera:")
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "TEMP IMAGE TITLE")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "TEMP IMAGE DESCRIPTION")

        imageUri = mContext.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if(it.resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "cameraActivityLauncher: Camera Image: $imageUri")
            saveImageTopAppLevelDirectory(imageUri!!)

            //notrify the adapter that a new image is inserted
            val modelImage = ModelImage(imageUri!!,false)
            allImageArrayList.add(modelImage)
            adapterImage.notifyItemInserted(allImageArrayList.size)
        } else {
            Log.d(TAG, "cameraActivityLauncher: Cancelled")
            toast("Cancelled...")
        }
    }


    private fun checkStoragePermission(): Boolean {
        Log.d(TAG, "checkStoragePermission: ")
        return ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private var requestStoragePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
        ActivityResultCallback <Boolean>(){isGranted ->
            Log.d(TAG, "requestStoragePermission: isGranted $isGranted ")

            if(isGranted) {
                pickImageGallery()
            } else {
                toast("Permission denied..")
            }
        }
    )


    private fun checkCameraPermission(): Boolean {
        Log.d(TAG, "checkCameraPermission: ")
        val cameraPerm = ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val storagePerm = ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

        return cameraPerm && storagePerm
    }

    private var requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
        ActivityResultCallback <Map<String, Boolean>>{
            Log.d(TAG, "requestCamerPermissions: ")
            Log.d(TAG, "requestCameraPermission: $it")

            var areAllGranted = true
            for(isGranted in it.values) {
                Log.d(TAG, "requestCameraPermissions: isGranted: $isGranted ")
                areAllGranted = areAllGranted && isGranted
            }

            if(areAllGranted) {
                pickImageCamera()
                Log.d(TAG, "requestCamerPermissions: All permission granted....: ")
            } else {
                toast("ALl or ont of the Camera , Storage permission denied...")
            }
        }
    )



    private fun toast(message: String) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
    }


}