package com.example.mobiiliohjelmointi_muistio

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_second_screen.*
import java.io.ByteArrayOutputStream
import java.lang.Exception

class SecondScreen : AppCompatActivity() {

    var selectedImage : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second_screen)
    }

    val REQUEST_GALLERY = 1
    val REQUEST_CAMERA = 2

    fun selectImage(view: View) {
        val alert = AlertDialog.Builder(this)

        alert.setTitle("Kuva")
        alert.setMessage("Valitse kamera tai galleria")
        alert.setPositiveButton("Kamera") { dialogInterface: DialogInterface, i: Int ->
            camera(view)
        }
        alert.setNegativeButton("Galleria") { dialogInterface: DialogInterface, i: Int ->
            gallery(view)
        }
        alert.show()
    }

    fun camera(view: View) {
        if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 2)
        } else if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 3)
        } else {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                startActivityForResult(intent, REQUEST_CAMERA)
            } catch (e: ActivityNotFoundException) {
                // display error state to the user
            }
        }
    }

    fun gallery(view: View) {
        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 2)
        } else {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_GALLERY)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            val image = data.data

            try {
                selectedImage = MediaStore.Images.Media.getBitmap(this.contentResolver, image)
                addImage.setImageBitmap(selectedImage)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if(requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
            try {
                val selectedImage = data!!.extras?.get("data") as Bitmap
                addImage.setImageBitmap(selectedImage)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    fun saveNote(view: View) {

        val noteTitle = titleField.text.toString()
        val noteContent = contentField.text.toString()

        val outputStream = ByteArrayOutputStream()

        selectedImage?.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
        val byteArray = outputStream.toByteArray()

        try {
            val database = this.openOrCreateDatabase("Notes", Context.MODE_PRIVATE, null)
            database.execSQL("create table if not exists note(title varchar, image blob, content varchar)")
            val sqlString = "insert into note (title, image, content) values (?, ?)"
            val statement = database.compileStatement(sqlString)
            statement.bindString(1, noteTitle)
            statement.bindBlob(2, byteArray)
            statement.bindString(3, noteContent)
            statement.execute()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)

    }
}