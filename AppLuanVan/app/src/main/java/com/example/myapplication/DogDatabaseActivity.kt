package com.example.myapplication

import android.app.AlertDialog
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_dog_database.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dongcho.*
import kotlinx.android.synthetic.main.dongcho.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class DogDatabaseActivity : AppCompatActivity() {
    val urlGetDataDog:String = "http://ntd29011999.000webhostapp.com/getDog.php"
    val urlInsertDataDog:String = "http://ntd29011999.000webhostapp.com/insertDog.php"
    val urlUpdateDataDog:String = "http://ntd29011999.000webhostapp.com/updatedataDog.php"
    val urlDeleteDataDog:String = "http://ntd29011999.000webhostapp.com/deletedataDog.php"

    var arraycho : ArrayList<ChoDatabase> = ArrayList()
    lateinit var adapter: AdapterCustom
    var name = ""
    var day = ""
    var clor = ""
    var descrip = ""
    var id = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dog_database)
        val bundle = intent.extras
        var IdDevice: String = ""
        bundle?.let {
            IdDevice = bundle.getString("IdDevice") as String
        }
        val actionBar = supportActionBar
        actionBar?.hide()
        val btnAddDog = findViewById<ImageButton>(R.id.BtnAddDog)

        btnAddDog.setOnClickListener {
            val dialogDevice = AlertDialog.Builder(this@DogDatabaseActivity)
            val inflater = layoutInflater
            val dialogLayout =  inflater.inflate(R.layout.dialog_insert_dog, null)
            val edtName = dialogLayout.findViewById<EditText>(R.id.edtTenAddDog)
            val edtDay = dialogLayout.findViewById<EditText>(R.id.edtNgayAddDog)
            edtDay.setText("29-12-1999")
            val edtDescrip = dialogLayout.findViewById<EditText>(R.id.edtMotaAddDog)
            val edtColor = dialogLayout.findViewById<EditText>(R.id.edtMauAddDog)
            with(dialogDevice){
                setTitle("Dog Insert")
                setPositiveButton("OK"){ dialog, which ->
                    name = edtName.text.toString().trim()
                    clor = edtColor.text.toString().trim()
                    descrip = edtDescrip.text.toString().trim()
                    day = edtDay.text.toString().trim()
                    InsertDataDog().execute(urlInsertDataDog)
                    object : CountDownTimer(1000, 1000) {
                        override fun onFinish() {
                            // When timer is finished
                            // Execute your code here
                        }

                        override fun onTick(millisUntilFinished: Long) {
                            // millisUntilFinished    The amount of time until finished.
                        }
                    }.start()
                    GetDataDog().execute(urlGetDataDog)

                }
                setNegativeButton("Cancle"){ dialog, which ->

                }
                setView(dialogLayout)
            }
            dialogDevice.show()
        }
//        var cal=Calendar.getInstance()
//        var cal0: Calendar = Calendar.getInstance()
//        var cal1: Calendar = Calendar.getInstance()
//
//        cal1.set(2021,4,1)
//        cal0.set(2021,4,27)
//        arraycho.add(ChoDatabase("Su","nâu đỏ","Quản gia",cal))
//        arraycho.add(ChoDatabase("Tep","bò sữa","Gác cổng",cal0))
//        arraycho.add(ChoDatabase("Tom","bò xám","Hung dữ",cal1))
        if (IdDevice.equals("123"))
            GetDataDog().execute(urlGetDataDog)


        ListViewCho.setOnItemClickListener { parent, view, position, id ->
            Toast.makeText(this@DogDatabaseActivity, "item is Clicked", Toast.LENGTH_LONG).show()
        }




    }

    public fun DialogSuaCongViec(mangCho: java.util.ArrayList<ChoDatabase>, position: Int){
        var layoutinflater : LayoutInflater = LayoutInflater.from(this@DogDatabaseActivity)
        val dialogDog = AlertDialog.Builder(this@DogDatabaseActivity)
        val dialogLayout =  layoutinflater.inflate(R.layout.dialog_edit_dog, null)
        val edtDate = dialogLayout.findViewById<EditText>(R.id.editTextDate)

        var cal : Calendar = arraycho[position].ngaylenggiong
        var date=cal.time
        var sdf= SimpleDateFormat("dd-MM-yyyy")
        Toast.makeText(this@DogDatabaseActivity, sdf.format(date) + " - " + cal.time.toString() + " - " + position, Toast.LENGTH_LONG).show()
        edtDate.setText(sdf.format(date))
        with(dialogDog){
            setTitle("Dog Edit")
            setPositiveButton("OK"){ dialog, which ->
                id = arraycho[position].id
                sdf = SimpleDateFormat("dd-MM-yyyy")
                cal.time = sdf.parse(edtDate.text.toString()) // all done
                day = edtDate.text.toString().trim()
                arraycho[position].setNgayLenGiong(cal)
                UpdateDataDog().execute(urlUpdateDataDog)
                adapter.notifyDataSetChanged()
                ListViewCho.adapter = adapter
//                var ngaymoi: String = arraycho[position].ngaylenggiong.get(Calendar.DAY_OF_MONTH).toString()

            }
            setNegativeButton("Cancle"){ dialog, which ->
            }
            setView(dialogLayout)
        }
        dialogDog.show()

    }
    public fun DeleteDog(mangCho: java.util.ArrayList<ChoDatabase>, position: Int){
        id = mangCho[position].id.trim()
        DeleteDataDog().execute(urlDeleteDataDog)
        object : CountDownTimer(1500, 1000) {
            override fun onFinish() {
                // When timer is finished
                // Execute your code here
            }

            override fun onTick(millisUntilFinished: Long) {
                // millisUntilFinished    The amount of time until finished.
            }
        }.start()
        GetDataDog().execute(urlGetDataDog)
    }

    inner class GetDataDog : AsyncTask<String, Void, String>(){
        override fun doInBackground(vararg params: String?): String {
            return getContentURL(params[0])
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
//            Toast.makeText(applicationContext, result, Toast.LENGTH_LONG).show()
            var jsonArray: JSONArray = JSONArray(result)
            var id:String =""
            var ten:String = ""
            var mau:String =""
            var ngaylengiong:String =""
            var mota:String =""
            arraycho.clear()
            var sdf = SimpleDateFormat("dd-MM-yyyy")
            for (moiTruong in 0..jsonArray.length()-1){
                var objectMoiTr: JSONObject = jsonArray.getJSONObject(moiTruong)
                id = objectMoiTr.getString("id")
                ten = objectMoiTr.getString("ten")
                ngaylengiong = objectMoiTr.getString("ngay")
                mau = objectMoiTr.getString("mau")
                mota = objectMoiTr.getString("mota")
                var caltemp : Calendar = Calendar.getInstance()
                caltemp.time = sdf.parse(ngaylengiong) // all done
                arraycho.add(ChoDatabase(id, ten, mau, mota, caltemp))
            }
            adapter = AdapterCustom(this@DogDatabaseActivity, arraycho)
            ListViewCho.adapter = adapter

        }

    }
    private fun getContentURL(url: String?) : String{
        var content: StringBuilder = StringBuilder();
        val url: URL = URL(url)
        val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
        val inputStreamReader: InputStreamReader = InputStreamReader(urlConnection.inputStream)
        val bufferedReader: BufferedReader = BufferedReader(inputStreamReader)

        var line: String = ""
        try {
            do {
                line = bufferedReader.readLine()
                if(line != null){
                    content.append(line)
                }
            }while (line != null)
            bufferedReader.close()
        }catch (e: Exception){
            Log.d("AAA", e.toString())
        }
        return content.toString()
    }

    inner class InsertDataDog : AsyncTask<String, Void, String>(){
        override fun doInBackground(vararg params: String?): String {
            return postDataDog(params[0])
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result.equals("success"))
                Toast.makeText(applicationContext, name + " & " + day + " & " + clor, Toast.LENGTH_LONG).show()
            else Toast.makeText(applicationContext, "THAT BAI", Toast.LENGTH_LONG).show()
        }

    }

    inner class UpdateDataDog : AsyncTask<String, Void, String>(){
        override fun doInBackground(vararg params: String?): String {
            return postDataDogForUpdate(params[0])
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result.equals("success"))
                Toast.makeText(applicationContext, id + " & " + day, Toast.LENGTH_LONG).show()
            else Toast.makeText(applicationContext, "THAT BAI", Toast.LENGTH_LONG).show()
        }

    }


    private fun postDataDogForUpdate(link: String?): String {
        val connect: HttpURLConnection
        var url: URL =  URL(link)
        try {
            connect = url.openConnection() as HttpURLConnection
            connect.readTimeout = 10000
            connect.connectTimeout = 15000
            connect.requestMethod = "POST"
            // POST theo tham số
            val builder = Uri.Builder()
                    .appendQueryParameter("id", id)
                    .appendQueryParameter("ngay", day)
            val query = builder.build().getEncodedQuery()
            val os = connect.outputStream
            val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
            writer.write(query)
            writer.flush()
            writer.close()
            os.close()
            connect.connect()
        } catch (e1: IOException) {
            e1.printStackTrace()
            return "Error!"
        }

        try {
            // Đọc nội dung trả về sau khi thực hiện POST
            val response_code = connect.responseCode
            if (response_code == HttpURLConnection.HTTP_OK) {
                val input = connect.inputStream
                val reader = BufferedReader(InputStreamReader(input))
                val result = StringBuilder()
                var line: String
                try {
                    do{
                        line = reader.readLine()
                        if(line != null){
                            result.append(line)
                        }
                    }while (line != null)

                    reader.close()
                }catch (e: Exception){}

                return result.toString()
            } else {
                return "Error!"
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return "Error!"
        } finally {
            connect.disconnect()
        }
    }


    inner class DeleteDataDog : AsyncTask<String, Void, String>(){
        override fun doInBackground(vararg params: String?): String {
            return postDataDogForDelete(params[0])
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result.equals("success"))
                Toast.makeText(applicationContext, id + "", Toast.LENGTH_LONG).show()
            else Toast.makeText(applicationContext, "THAT BAI", Toast.LENGTH_LONG).show()
        }

    }


    private fun postDataDogForDelete(link: String?): String {
        val connect: HttpURLConnection
        var url: URL =  URL(link)
        try {
            connect = url.openConnection() as HttpURLConnection
            connect.readTimeout = 10000
            connect.connectTimeout = 15000
            connect.requestMethod = "POST"
            // POST theo tham số
            val builder = Uri.Builder()
                    .appendQueryParameter("id", id)
            val query = builder.build().getEncodedQuery()
            val os = connect.outputStream
            val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
            writer.write(query)
            writer.flush()
            writer.close()
            os.close()
            connect.connect()
        } catch (e1: IOException) {
            e1.printStackTrace()
            return "Error!"
        }

        try {
            // Đọc nội dung trả về sau khi thực hiện POST
            val response_code = connect.responseCode
            if (response_code == HttpURLConnection.HTTP_OK) {
                val input = connect.inputStream
                val reader = BufferedReader(InputStreamReader(input))
                val result = StringBuilder()
                var line: String
                try {
                    do{
                        line = reader.readLine()
                        if(line != null){
                            result.append(line)
                        }
                    }while (line != null)

                    reader.close()
                }catch (e: Exception){}

                return result.toString()
            } else {
                return "Error!"
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return "Error!"
        } finally {
            connect.disconnect()
        }
    }
    private fun postDataDog(link: String?): String {
        val connect: HttpURLConnection
        var url: URL =  URL(link)
        try {
            connect = url.openConnection() as HttpURLConnection
            connect.readTimeout = 10000
            connect.connectTimeout = 15000
            connect.requestMethod = "POST"
            // POST theo tham số
            val builder = Uri.Builder()
                    .appendQueryParameter("ten", name)
                    .appendQueryParameter("ngay", day)
                    .appendQueryParameter("mau", clor)
                    .appendQueryParameter("mota", descrip)
            val query = builder.build().getEncodedQuery()
            val os = connect.outputStream
            val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
            writer.write(query)
            writer.flush()
            writer.close()
            os.close()
            connect.connect()
        } catch (e1: IOException) {
            e1.printStackTrace()
            return "Error!"
        }

        try {
            // Đọc nội dung trả về sau khi thực hiện POST
            val response_code = connect.responseCode
            if (response_code == HttpURLConnection.HTTP_OK) {
                val input = connect.inputStream
                val reader = BufferedReader(InputStreamReader(input))
                val result = StringBuilder()
                var line: String
                try {
                    do{
                        line = reader.readLine()
                        if(line != null){
                            result.append(line)
                        }
                    }while (line != null)

                    reader.close()
                }catch (e: Exception){}

                return result.toString()
            } else {
                return "Error!"
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return "Error!"
        } finally {
            connect.disconnect()
        }
    }

}