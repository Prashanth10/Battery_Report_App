package com.example.battery_report_android

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object{
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "battery.db"
        private const val TABLE_NAME = "battery_report"
        private const val PERCENT = "percent"
        private const val TIME = "time"
        private const val CHARGE_STATUS = "charge_status"
    }
    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = "CREATE TABLE battery_report(percent INTEGER, time TEXT, charge_status TEXT)"
//        val createTable = ("CREATE TABLE "+ TABLE_NAME +"("
//                + PERCENT +" INTEGER,"+ TIME +" TEXT,"
//                + CHARGE_STATUS +" TEXT"+")")
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertData(data: BatteryData): Long{
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(PERCENT, data.batteryPercent)
        contentValues.put(TIME, data.curTime)
        contentValues.put(CHARGE_STATUS, data.chargeStatus)

        val success = db.insert(TABLE_NAME, null, contentValues)
        db.close()
        return success
    }

    @SuppressLint("Range")
    fun getAllData(): ArrayList<BatteryData>{
        val dataList : ArrayList<BatteryData> = ArrayList()
        val selectQuery = "SELECT * FROM $TABLE_NAME"
        val db = this.readableDatabase

        val cursor : Cursor?

        try{
            cursor = db.rawQuery(selectQuery, null)
        }catch (e: Exception){
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var percentage: Int
        var time: String
        var batteryStatus: String

        if(cursor.moveToFirst()){
            do{
                percentage = cursor.getInt(cursor.getColumnIndex("percent"))
                time = cursor.getString(cursor.getColumnIndex("time"))
                batteryStatus = cursor.getString(cursor.getColumnIndex("charge_status"))

                val bat = BatteryData(percentage, time, batteryStatus)
                dataList.add(bat)
            }while (cursor.moveToNext())
        }
        return dataList
    }
}