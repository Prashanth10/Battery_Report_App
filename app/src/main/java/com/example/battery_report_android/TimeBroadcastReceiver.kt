package com.example.battery_report_android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*


class TimeBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, timerIntent: Intent?) {

        val time = Calendar.getInstance().time
        val formatter = SimpleDateFormat("hh:mm:ss") //or use getDateInstance()
        val formatedTime = formatter.format(time)

        if(formatedTime.contains(":00:00")){   //formatedTime.contains(":00:00")
            val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus: Intent? = context?.registerReceiver(null, iFilter)

            var chargelevel = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            var batStatus = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            var chargeStatus: String
            if(batStatus == BatteryManager.BATTERY_STATUS_CHARGING){
                chargeStatus = "Charging"
            }else{
                chargeStatus = "Discharging"
            }
            val bat = BatteryData(chargelevel,formatedTime,chargeStatus)
            var sqliteHelper:DatabaseHelper = DatabaseHelper(context.applicationContext)
            val status = sqliteHelper.insertData(bat)
            if(status>-1){
                Log.i("Timer Data added", "$bat")
            }else{
                Log.i("Timer not saved", "$bat")
            }

        }
    }
}