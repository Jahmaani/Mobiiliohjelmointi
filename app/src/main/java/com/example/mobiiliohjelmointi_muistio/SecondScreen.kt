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
import android.icu.util.UniversalTimeScale.toLong
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log.d
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_second_screen.*
import java.io.ByteArrayOutputStream
import java.lang.Exception

class SecondScreen : AppCompatActivity() {

    //Kuvan muuttuja
    var selectedImage : Bitmap? = null
    var id_item : Int? = null

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
            //Poista ja update buttonit piiloon
            updateBtn.visibility = View.INVISIBLE
            deleteBtn.visibility = View.INVISIBLE

            //Tittlefieldi tyhjäksi
            titleField.setText("")
        }
        //Näytetään muokkaus
        else {
            //Otetaan putextran valuet muuttujaan
            val title = intent.getStringExtra("title")
            val content = intent.getStringExtra("content")
            id_item = intent.getIntExtra("id", -1)

            // Lisätään arvot kenttiin
            titleField.setText(title)
            contentField.setText(content)

            val chosen = Globals.Chosen
            //Haetaan kuva globaalista classista
            if (chosen.chosenImage == null) {//POISTOON
                addImage.setImageBitmap(BitmapFactory.decodeResource(applicationContext.resources,R.drawable.addimage))
            } else {
                val bitmap = chosen.returnImage()
                addImage.setImageBitmap(bitmap)
            }

            //Piilotetaan tallenna näppäin
            saveBtn.visibility = View.INVISIBLE
            updateBtn.visibility = View.INVISIBLE

            //Katsotaan onko otsikko kenttä muuttunut
            titleField.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable) {
                    updateBtn.visibility = View.VISIBLE

                }

                override fun beforeTextChanged(s: CharSequence, start: Int,
                                               count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence, start: Int,
                                           before: Int, count: Int) {
                }
            })

            //Katsotaan onko contentti kenttä muuttunut
            contentField.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable) {
                    updateBtn.visibility = View.VISIBLE

                }

                override fun beforeTextChanged(s: CharSequence, start: Int,
                                               count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence, start: Int,
                                           before: Int, count: Int) {
                }
            })

            addImage.setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    when (event?.action) {
                        MotionEvent.ACTION_DOWN -> updateBtn.visibility = View.VISIBLE
                    }

                    return v?.onTouchEvent(event) ?: true
                }
            })
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
            // database.execSQL("create table if not exists note(title varchar, image blob, content varchar)")
            database.execSQL("create table if not exists note(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title varchar, image blob, content varchar)")
            //SQL String tyhjillä muuttujilla
            val sqlString = "insert into note (ID, title, image, content) values (?, ?, ?, ?)"
            println("sqlstring: " + sqlString)

            val statement = database.compileStatement(sqlString)
            //Lisätään sql lauseeseen muuttujat
            statement.bindNull(1)
            statement.bindString(2, noteTitle)
            statement.bindBlob(3, byteArray)
            statement.bindString(4, noteContent)
            println("Statement: $statement")


            //tehdään työ
            statement.execute()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        //Kun nämä on tehty, niin palataan MainActivity näyttöön.
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)

    }

    // Muistiinpanon poisto

    fun deleteNote(view: View) {

        try {
            //Avataan database
            val database = this.openOrCreateDatabase("Notes", Context.MODE_PRIVATE, null)
            database.execSQL("create table if not exists note(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title varchar, image blob, content varchar)")
            val sqlString = "delete from note where id=$id_item"

            val statement = database.compileStatement(sqlString)
            statement.execute()
        }
        catch (e: Exception) {
            e.printStackTrace()
        }

        //Kun nämä on tehty, niin palataan MainActivity näyttöön.
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)


    }

    fun updateNote(view: View) {
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
            database.execSQL("create table if not exists note(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title varchar, image blob, content varchar)")
            val sqlDeleteString = "delete from note where id=$id_item"
            val statementDelete = database.compileStatement(sqlDeleteString)
            statementDelete.execute()

            val sqlString = "insert into note (ID, title, image, content) values (?, ?, ?, ?)"

            val statement = database.compileStatement(sqlString)
            //Lisätään sql lauseeseen muuttujat
            statement.bindLong(1, id_item!!.toLong())
            statement.bindString(2, noteTitle)
            statement.bindBlob(3, byteArray)
            statement.bindString(4, noteContent)


            statement.execute()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        //Kun nämä on tehty, niin palataan MainActivity näyttöön.
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)

    }
}