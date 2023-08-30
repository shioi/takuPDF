package com.example.takupdf.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.takupdf.R

class ImageViewActivity : AppCompatActivity() {

    private lateinit var imageIv: ImageView;
    private var imageUri = ""

    companion object {
        private const val TAG = "IMAGE_VIEW_TAG"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)

        supportActionBar?.title = "Image View"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        imageIv = findViewById(R.id.imageIv)

        imageUri = intent.getStringExtra("imageUri").toString()
        Log.d(TAG, "Onincrease: imageUri: $imageUri")
        registerForContextMenu(imageIv)

        Glide.with(this)
            .load(imageUri)
            .placeholder(R.drawable.ic_image_add)
            .into(imageIv)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.detect_text -> {
                val i = Intent(this, RecognizeTextActivity::class.java)
                i.putExtra("imageUri", imageUri)
                startActivity(i)
            }
            else -> return super.onContextItemSelected(item)
        }
        return true
    }
}