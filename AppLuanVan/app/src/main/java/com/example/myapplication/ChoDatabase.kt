package com.example.myapplication

import java.util.*

data class ChoDatabase(var id: String,var ten: String, var mau: String, var mota: String, var ngaylenggiong: Calendar) {
    fun setNgayLenGiong(ngay: Calendar){
        ngaylenggiong = ngay
    }
}