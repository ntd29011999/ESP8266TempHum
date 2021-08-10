package com.example.myapplication

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class SignUpActivity : AppCompatActivity() {
    val urlUpdateAccount:String = "http://ntd29011999.000webhostapp.com/insertaccount.php"
    var phone = ""
    var password = ""
    var deviceid = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        val actionBar = supportActionBar
        actionBar?.hide()
        val edtPhone = findViewById<EditText>(R.id.editTextPhone)
        val edtPassword = findViewById<EditText>(R.id.editTextTextPersonName2)
        val edtDeviceID = findViewById<EditText>(R.id.editTextTextPersonName3)
        val btnSignUp = findViewById<Button>(R.id.buttonSignUp)
        val btnToLogin = findViewById<Button>(R.id.buttonToLoginActivity)

        btnToLogin.setOnClickListener {
            val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
            startActivity(intent)
        }
        btnSignUp.setOnClickListener {


            var invalidFlag: Boolean = false;
            val passwordPattern1 = "[a-z]{8,32}".toRegex()
            val passwordPattern2 = "[!|@|#|$|%|^|&|*|(|)|-|_|+|=]".toRegex()
            val passwordPattern3 = "[A-Z]".toRegex()
            val passwordPattern4 = "[0-9]".toRegex()


            if(passwordPattern1.containsMatchIn(edtPassword.text.trim().toString())
                &&passwordPattern2.containsMatchIn(edtPassword.text.trim().toString())
                &&passwordPattern3.containsMatchIn(edtPassword.text.trim().toString())
                &&passwordPattern4.containsMatchIn(edtPassword.text.trim().toString())){
                invalidFlag = false;
            }
            else{
                edtPassword.setError("The password is invalid");
                invalidFlag = true;
            }

            if (edtPhone.text.trim().toString().isNullOrEmpty()) {
                edtPhone.setError("enter Phone please!");
            }
            if (edtDeviceID.text.trim().toString().isNullOrEmpty()) {
                edtDeviceID.setError("enter Device ID please!");
            }
            if (edtPassword.text.trim().toString().isNullOrEmpty()) {
                edtPassword.setError("enter Password please!");
            }
            if (edtPhone.text.trim().toString() != "" && (edtDeviceID.text.trim().toString().equals("123") || edtDeviceID.text.trim().toString().equals("456")) && edtPassword.text.trim().toString() != "" && invalidFlag == false){
                phone = edtPhone.text.toString().trim()
                password = edtPassword.text.toString().trim()
                deviceid = edtDeviceID.text.toString().trim()
                UpdateData().execute(urlUpdateAccount)
                val intent = Intent(this@SignUpActivity,LoginActivity::class.java)
                startActivity(intent)

            }else{
                val alertDialogBuilder = android.app.AlertDialog.Builder(this@SignUpActivity)
                alertDialogBuilder.setMessage("failed sign up!")
                alertDialogBuilder.setPositiveButton("OK") { dialog: DialogInterface,
                                                             which: Int ->
                    Toast.makeText(applicationContext, "please try again!", Toast.LENGTH_SHORT).show()
                }
                alertDialogBuilder.show()
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
                Toast.makeText(applicationContext, "", Toast.LENGTH_LONG).show()
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
                .appendQueryParameter("phone", phone)
                .appendQueryParameter("password", password)
                .appendQueryParameter("deviceid", deviceid)
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
}