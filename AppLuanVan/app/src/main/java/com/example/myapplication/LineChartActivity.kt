package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*

class LineChartActivity : AppCompatActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.line_chart)
        val bundle = intent.extras
        var IdDevice: String = ""
        var tvNgay = findViewById<TextView>(R.id.tvNgayDoND)
        var mangND:ArrayList<String> = ArrayList()
        var mangTg:ArrayList<String> = ArrayList()
        var mangNgay:ArrayList<String> = ArrayList()
        var mangX:ArrayList<String> = ArrayList()
        bundle?.let {
            mangND = bundle.getStringArrayList("NhietDo") as ArrayList<String>
            mangTg = bundle.getStringArrayList("ThoiGian") as ArrayList<String>
            mangNgay = bundle.getStringArrayList("Ngay") as ArrayList<String>
            IdDevice = bundle.getString("IdDevice") as String
        }
        if (IdDevice.equals("123")) {
            for (i in 0 until 144) {
                mangX.add(mangTg[i] + "(" + mangNgay[i] + ")")
            }
            tvNgay.text = mangX[142] + " - " + mangX[0]
            setLineChartData(mangND,mangX)
        }

    }


    fun setLineChartData(mangND:ArrayList<String>,mangTg:ArrayList<String>){
        val lineChart = findViewById<LineChart>(R.id.lineChart)
        val xvalue = ArrayList<String>()

        for (i in 0 until mangTg.size-1) {
            xvalue.add(mangTg[mangTg.size-1-i])
        }


        val yentries = ArrayList<Entry>()
        for (i in 0 until mangND.size-1) {
            yentries.add(Entry(mangND[143-i].toFloat(),i))
        }

        val linedataset1 = LineDataSet(yentries,"NhietDo")
        linedataset1.color = resources.getColor(R.color.purple_700)
        linedataset1.lineWidth = 2f
        linedataset1.valueTextSize = 20f
        val data = LineData(xvalue,linedataset1)

        lineChart.data = data
        lineChart.zoom(1f,1.5f,3f,2f)
        lineChart.setBackgroundColor(resources.getColor(R.color.white))
        lineChart.animateXY(3000,3000)
    }
}