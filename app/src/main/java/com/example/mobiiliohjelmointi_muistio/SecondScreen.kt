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
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_second_screen.*
import java.io.ByteArrayOutputStream
import java.lang.Exception

class SecondScreen : AppCompatActivity() {

    //Kuvan muuttuja
    var selectedImage : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second_screen)

        val intent = intent

        //Otetaan putextran info value muuttujaan
        val info = intent.getStringExtra("info")

        //Luodaan näkymä lisäykselle
        if (info.equals("new")) {
            //Otetaan placeholder kuva muuttujaan
            val background = BitmapFactory.decodeResource(applicationContext.resources,R.drawable.addimage)
            //Annetaan placeholderkuva elementille
            addImage.setImageBitmap(background)
            //Tallenna näppäin näkyväksi
            saveBtn.visibility = View.VISIBLE
            //Tittlefieldi tyhjäksi
            titleField.setText("")
        }
        //Näytetään lisätty
        else {
            //Otetaan putextran title value muuttujana
            val title = intent.getStringExtra("title")
            //TODO content
            
            //Lisätään title fieldiin otsikko
            titleField.setText(title)

            //Haetaan kuva globaalista classista
            val chosen = Globals.Chosen
            val bitmap = chosen.returnImage()

            //Lisätään kuva elementtiin
            addImage.setImageBitmap(bitmap)
            //Piilotetaan tallenna näppäin
            saveBtn.visibility = View.INVISIBLE
        }
    }

    //Annetaan kameralle ja gallerialle request numero, jolla yksilöidään tapahtuma
    val REQUEST_GALLERY = 1
    val REQUEST_CAMERA = 2

    //Luodaan alert message
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
        //Tässä kysytään kameralle lupia
        if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 2)
        }
        //Tässä kysytään lupia kirjoittaa puhelimen tallennustilaan
        else if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 3)
        }
        //Jos luvat on annettu pyydetään kameran aukaisua
        else {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                startActivityForResult(intent, REQUEST_CAMERA)
            } catch (e: ActivityNotFoundException) {
                // display error state to the user
            }
        }
    }

    fun gallery(view: View) {
        //Kysytään gallerialle lupia
        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 2)
        }
        //Jos luvat on annettu pyydetään gallerian aukaisua
        else {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_GALLERY)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        //Jos requestcode on 1 avataan galleria, jos se on 2 niin kamera.
        if(requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            val image = data.data

            try {
                //Galleriasta valittu kuva lisätään muuttujaan selectImage
                selectedImage = MediaStore.Images.Media.getBitmap(this.contentResolver, image)
                //Asetetaan kuva elementtiin
                addImage.setImageBitmap(selectedImage)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if(requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
            try {
                //Kameralla otettu kuva lisätään muuttujaan
                selectedImage = data!!.extras?.get("data") as Bitmap
                //Asetetaan kuva elementtiin
                addImage.setImageBitmap(selectedImage)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    //Tietojen tallennus
    fun saveNote(view: View) {

        //Otetaan title ja content muuttujiin
        val noteTitle = titleField.text.toString()
        val noteContent = contentField.text.toString()
        val outputStream = ByteArrayOutputStream()

        //Pakataan kuva
        selectedImage?.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
        //Lisätään kuva bytearray muuttujaan.
        val byteArray = outputStream.toByteArray()

        try {
            //Luodaan database
            val database = this.openOrCreateDatabase("Notes", Context.MODE_PRIVATE, null)
            //Luodaan taulut
            database.execSQL("create table if not exists note(title varchar, image blob, content varchar)")
            //SQL String tyhjillä muuttujilla
            val sqlString = "insert into note (title, image, content) values (?, ?, ?)"
            val statement = database.compileStatement(sqlString)
            //Lisätään sql lauseeseen muuttujat
            statement.bindString(1, noteTitle)
            statement.bindBlob(2, byteArray)
            statement.bindString(3, noteContent)
            //tehdään työ
            statement.execute()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        //Kun nämä on tehty, niin palataan MainActivity näyttöön.
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)

    }
}