package com.example.takupdf.activities

import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.takupdf.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions


class RecognizeTextActivity : AppCompatActivity() {
    private lateinit var progressDialog: ProgressDialog

    private lateinit var textRecognizer: TextRecognizer
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recognize_text)

        val imageUri = intent.getStringExtra("imageUri")

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //for text
        textView = findViewById<TextView>(R.id.recognisedText)

        registerForContextMenu(textView)

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        progressDialog.show()

        //fab
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, textView.text.toString())
            sendIntent.type = "text/plain"
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)

        }

        try {
            val inputImage = InputImage.fromFilePath(this, Uri.parse(imageUri))
            val textTaskResult = textRecognizer.process(inputImage)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    val recognizedText = it.text
                    textView.text = recognizedText
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Failed to recognize text due to ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Toast.makeText(this, "failed to prepare image ${e.message}", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.copy_context, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.copy -> {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip: ClipData = ClipData.newPlainText("Recognized Text",textView.text.toString())
                clipboard.setPrimaryClip(clip)
                Toast.makeText(
                    applicationContext, "Copied Text", Toast.LENGTH_SHORT)
                    .show()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}