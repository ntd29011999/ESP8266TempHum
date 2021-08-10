package com.example.myapplication

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.provider.CalendarContract.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_dog_database.*
import java.text.SimpleDateFormat
import java.util.*

class AdapterCustom(var context: DogDatabaseActivity, var mangCho: ArrayList<ChoDatabase>) : BaseAdapter() {
    class ViewHolder(row: View){
        var tvID : TextView
        var tvCho : TextView
        var tvMau : TextView
        var tvMota : TextView
        var tvDate : TextView
        var imgButtonDelete : ImageButton
        var imgButtonEdit : ImageButton
        init{
            tvID = row.findViewById(R.id.tvIDDog)
            tvCho = row.findViewById(R.id.textViewCho)
            tvMau = row.findViewById(R.id.tvMau)
            tvMota = row.findViewById(R.id.tvMota)
            tvDate = row.findViewById(R.id.tvDate)
            imgButtonDelete = row.findViewById(R.id.imageButtonDelete)
            imgButtonEdit = row.findViewById(R.id.imageButtonEdit)
        }
    }
    override fun getCount(): Int {
        return mangCho.size

    }

    override fun getItem(position: Int): Any {
        return mangCho.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view : View?
        var viewholder : ViewHolder
        if (convertView == null){
            var layoutinflater : LayoutInflater = LayoutInflater.from(context)
            view = layoutinflater.inflate(R.layout.dongcho, null)
            viewholder = ViewHolder(view)
            view.tag = viewholder

        }
        else {
            view = convertView
            viewholder = convertView.tag as ViewHolder
        }
        var cho : ChoDatabase = getItem(position) as ChoDatabase
        viewholder.tvID.text = cho.id
        viewholder.tvCho.text = cho.ten
        viewholder.tvMau.text = cho.mau
        viewholder.tvMota.text = cho.mota
        viewholder.tvDate.text = cho.ngaylenggiong.get(Calendar.DAY_OF_MONTH).toString() + "-" + (cho.ngaylenggiong.get(Calendar.MONTH)+1).toString()+ "-" + cho.ngaylenggiong.get(Calendar.YEAR).toString()
        viewholder.imgButtonEdit.setOnClickListener {
            context.DialogSuaCongViec(mangCho,position)
        }
        viewholder.imgButtonDelete.setOnClickListener {
            context.DeleteDog(mangCho,position)
        }
        return view as View
    }
}