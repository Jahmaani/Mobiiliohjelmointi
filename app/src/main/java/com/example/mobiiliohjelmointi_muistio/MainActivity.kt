package com.example.mobiiliohjelmointi_muistio

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log.d
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
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
        val idArray = ArrayList<Int>()


        //val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, titleArray)

        val arrayAdapter: ArrayAdapter<String> =
            object : ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titleArray) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val v = super.getView(position, convertView, parent)
                    val tv = v.findViewById<TextView>(android.R.id.text1)
                    tv.setTextColor(Color.parseColor("#968C83"))
                    return v
                }
            }

        listView.adapter = arrayAdapter

        try {
            //Luodaan database
            val database = this.openOrCreateDatabase("Notes", Context.MODE_PRIVATE, null)
            //Luodaan taulut
            database.execSQL("create table if not exists note(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title varchar, image blob, content varchar)")

            //SQL query
            val cursor = database.rawQuery("select * from note", null)


            //Otetaan tiedot muuttujiin
            val nameidx = cursor.getColumnIndex("title")
            val imageidx = cursor.getColumnIndex("image")
            val contentidx = cursor.getColumnIndex("content")
            val idx = cursor.getColumnIndex("ID")


            //Hypätään ensimmäiselle riville
            cursor.moveToFirst()

            //Loopataan elementit läpi
            while (cursor != null) {
                //Lisätään otsikot ja content listaan
                titleArray.add(cursor.getString(nameidx))
                contentArray.add(cursor.getString(contentidx))
                idArray.add(cursor.getInt(idx))

                val byteArray = cursor.getBlob(imageidx)
                val image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                println("cursor.blobimg: "+cursor.getBlob(imageidx) + " kuva: $image, id: "+cursor.getInt(idx))
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
            intent.putExtra("id", idArray[i])
            intent.putExtra("title", titleArray[i])
            intent.putExtra("content", contentArray[i])
            intent.putExtra("info", "old")

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