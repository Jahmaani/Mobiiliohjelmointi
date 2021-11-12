package com.example.mobiiliohjelmointi_muistio

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val artNameArray = ArrayList<String>()
        val artImageArray = ArrayList<Bitmap>()

        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, artNameArray)

        listView.adapter = arrayAdapter

        try {
            //Luodaan database
            val database = this.openOrCreateDatabase("Notes", Context.MODE_PRIVATE, null)
            //Luodaan taulut
            database.execSQL("create table if not exists note(title varchar, image blob, content varchar)")

            //SQL query
            val cursor = database.rawQuery("select * from note", null)

            //Otetaan nimi muuttujaan
            val nameidx = cursor.getColumnIndex("title")

            //Otetaan kuva muuttujaan
            val imageidx = cursor.getColumnIndex("image")

            //Siirretään cursori ensimmäiseen elementtiin
            cursor.moveToFirst()

            //Loopataan elementit läpi
            while (cursor != null) {
                artNameArray.add(cursor.getString(nameidx))
                val byteArray = cursor.getBlob(imageidx)
                val image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

                artImageArray.add(image)

                cursor.moveToNext()

                arrayAdapter.notifyDataSetChanged()

            }

            cursor?.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        listView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val intent = Intent(applicationContext, SecondScreen::class.java)
            intent.putExtra("name", artNameArray[i])
            intent.putExtra("info", "old")

            val chosen = Globals.Chosen
            chosen.chosenImage = artImageArray[i]

            startActivity(intent)
        }
    }

    //Luodaan menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.add_note, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //Lisää muistiinpanoa klikkaamalla tapahtuva asia
        if (item.itemId == R.id.add_note) {
            val intent = Intent(applicationContext, SecondScreen::class.java)
            intent.putExtra("info", "new")
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }
}