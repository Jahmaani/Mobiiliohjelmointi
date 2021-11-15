package com.example.mobiiliohjelmointi_muistio

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log.d
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

        // Luodaan tiedoille listat
        val titleArray = ArrayList<String>()
        val artImageArray = ArrayList<Bitmap>()
        val contentArray = ArrayList<String>()


        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, titleArray)

        listView.adapter = arrayAdapter

        try {
            //Luodaan database
            val database = this.openOrCreateDatabase("Notes", Context.MODE_PRIVATE, null)
            //Luodaan taulut
            database.execSQL("create table if not exists note(id integer primary key autoincrement, title varchar, image blob, content varchar)")

            //SQL query
            val cursor = database.rawQuery("select * from note", null)


            //Otetaan tiedot muuttujiin
            val nameidx = cursor.getColumnIndex("title")
            val imageidx = cursor.getColumnIndex("image")
            val contentidx = cursor.getColumnIndex("content")

            //Hypätään ensimmäiselle riville
            cursor.moveToFirst()

            //Loopataan elementit läpi
            while (cursor != null) {
                //Lisätään otsikot ja content listaan
                titleArray.add(cursor.getString(nameidx))
                contentArray.add(cursor.getString(contentidx))

                val byteArray = cursor.getBlob(imageidx)
                val image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                //Lisätään kuvat listaan
                artImageArray.add(image)
                //Hypätään seuraavaan riviin
                cursor.moveToNext()
                //En tiedä mitä tekee
                arrayAdapter.notifyDataSetChanged()



            }
            //Lopetetaan tietokannan haku
            cursor?.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }


        // i = position, l = id
        listView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            //Kutsutaan secondscreeniä
            val intent = Intent(applicationContext, SecondScreen::class.java)
            //Lisätään putextralle arvoja
            val id: Long = l.toLong()

            intent.putExtra("id", id)
            intent.putExtra("title", titleArray[i])
            intent.putExtra("content", contentArray[i])
            intent.putExtra("info", "old")

            d("smv", i.toString())
            d("smv", l.toString())

            //Luodaan olio
            val chosen = Globals.Chosen
            //Lisätään kuva taulukosta olioon
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