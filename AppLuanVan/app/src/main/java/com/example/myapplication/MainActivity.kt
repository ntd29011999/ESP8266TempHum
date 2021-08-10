package com.example.myapplication

import android.app.ActionBar
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.sql.Time
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    val urlGetData:String = "http://ntd29011999.000webhostapp.com/getdata.php"
    val urlGetDataDevice:String = "http://ntd29011999.000webhostapp.com/getdataDevice.php"
    val urlUpdateData:String = "http://ntd29011999.000webhostapp.com/updatedataDevice.php"
    var mangND: ArrayList<String> = ArrayList()
    var mangTg: ArrayList<String> = ArrayList()
    var mangNgay: ArrayList<String> = ArrayList()
    var IdDevice:String =""
    var state = ""
    var mode = ""
    var led = ""
    var tvDevice: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val bundle = intent.extras
        bundle?.let {
            IdDevice = bundle.getString("IdDevice") as String
        }
        val actionBar = supportActionBar
        actionBar?.hide()

        val tvNhietDo = findViewById<TextView>(R.id.tvnhietdo)
        val buttonLineChart = findViewById<Button>(R.id.lineChartButton)
        val buttonDevice = findViewById<Button>(R.id.deviceButton)
        val buttonDatabase = findViewById<Button>(R.id.buttonDatabase)


        GetDataDevice().execute(urlGetDataDevice)

        GetData().execute(urlGetData)
        buttonDevice.setOnClickListener(){
            val dialogDevice = AlertDialog.Builder(this@MainActivity)
            val inflater = layoutInflater
            val dialogLayout =  inflater.inflate(R.layout.dialog_edit_device,null)
            val switchState = dialogLayout.findViewById<Switch>(R.id.switchSTATE)
            val switchMode = dialogLayout.findViewById<Switch>(R.id.switchMODE)
            val switchLed = dialogLayout.findViewById<Switch>(R.id.switchLED)
            if (state.equals("ON"))
                switchState.toggle()
            if (mode.equals("AUTO"))
                switchMode.toggle()
            if (led.equals("ON"))
                switchLed.toggle()
            with(dialogDevice){
                setTitle("Device Config")
                setPositiveButton("OK"){dialog, which ->
                    mode = if (switchMode.isChecked) switchMode.textOn.toString().trim() else switchMode.textOff.toString().trim()
                    state = if (switchState.isChecked) switchState.textOn.toString().trim() else switchState.textOff.toString().trim()
                    led = if (switchLed.isChecked) switchLed.textOn.toString().trim() else switchLed.textOff.toString().trim()
                    if (IdDevice.equals("123")) {
                        UpdateData().execute(urlUpdateData)
                        tvDevice?.text = mode + " - " + state + " - " + led
                    }
                }
                setNegativeButton("Cancle"){dialog, which ->

                }
                setView(dialogLayout)
            }
            dialogDevice.show()

        }
        buttonLineChart.setOnClickListener {
            val intent = Intent(this@MainActivity,LineChartActivity::class.java)
            val bundle = Bundle()
            bundle.putString("IdDevice",IdDevice)
            bundle.putStringArrayList("NhietDo",mangND)
            bundle.putStringArrayList("ThoiGian",mangTg)
            bundle.putStringArrayList("Ngay",mangNgay)
            intent.putExtras(bundle)
            startActivity(intent)
        }
        buttonDatabase.setOnClickListener {
            val intent = Intent(this@MainActivity,DogDatabaseActivity::class.java)
            val bundle = Bundle()
            bundle.putString("IdDevice",IdDevice)
            intent.putExtras(bundle)
            startActivity(intent)
        }

    }

    inner class GetData : AsyncTask<String, Void, String>(){
        override fun doInBackground(vararg params: String?): String {
            return getContentURL(params[0])
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            var jsonArray: JSONArray = JSONArray(result)
            var id:String =""
            var nhietdo:String = ""
            var doam:String =""
            var mota:String =""
            var Ngay:String=""
            var Thoigian:String=""

            for (moiTruong in 0..jsonArray.length()-1){
                var objectMoiTr: JSONObject = jsonArray.getJSONObject(moiTruong)
                id = objectMoiTr.getString("id")
                nhietdo = objectMoiTr.getString("nhietdo")
                doam = objectMoiTr.getString("doam")
                mota = objectMoiTr.getString("mota")
                Ngay = objectMoiTr.getString("Ngay")
                Thoigian= objectMoiTr.getString("Thoigian")
                mangND.add(nhietdo)
                mangTg.add(Thoigian)
                mangNgay.add(Ngay)
            }

            if (IdDevice.equals("123"))
            tvnhietdo.text = mangND[0] + " ºC"
            else tvnhietdo.text = "error ºC"

        }

    }
    inner class GetDataDevice : AsyncTask<String, Void, String>(){
        override fun doInBackground(vararg params: String?): String {
            return getContentURL(params[0])
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            var jsonArray: JSONArray = JSONArray(result)
            var id:String =""
            for (device in 0..jsonArray.length()-1){
                var objectMoiTr: JSONObject = jsonArray.getJSONObject(device)
                id = objectMoiTr.getString("id")
                state = objectMoiTr.getString("battat")
                mode = objectMoiTr.getString("chedo")
                led = objectMoiTr.getString("led")
                tvDevice?.text = mode +" - "+ state +" - "+ led

            }


        }

    }

    inner class UpdateData : AsyncTask<String, Void, String>(){
        override fun doInBackground(vararg params: String?): String {
            return postData(params[0])
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result.equals("success"))
                Toast.makeText(applicationContext, mode+" & "+state+" & "+led, Toast.LENGTH_LONG).show()
            else Toast.makeText(applicationContext, "THAT BAI", Toast.LENGTH_LONG).show()
        }

    }

    private fun postData(link: String?): String {
        val connect: HttpURLConnection
        var url: URL =  URL(link)
        try {
            connect = url.openConnection() as HttpURLConnection
            connect.readTimeout = 10000
            connect.connectTimeout = 15000
            connect.requestMethod = "POST"
            // POST theo tham số
            val builder = Uri.Builder()
                .appendQueryParameter("battat", state)
                .appendQueryParameter("chedo", mode)
                .appendQueryParameter("led", led)
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
                }catch (e:Exception){}

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

}