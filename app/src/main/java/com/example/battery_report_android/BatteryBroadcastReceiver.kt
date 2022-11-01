package com.example.battery_report_android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class BatteryBroadcastReceiver : BroadcastReceiver() {
    var charging: Boolean? = null

    override fun onReceive(context: Context?, batteryIntent: Intent?) {
        var chargelevel = batteryIntent!!.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
        var status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        var chargeStatus: String
        if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
            chargeStatus = "Charging"
        } else {
            chargeStatus = "Discharging"
        }
        val time = Calendar.getInstance().time
        val formatter = SimpleDateFormat("hh:mm:ss") //or use getDateInstance()
        val formatedDate = formatter.format(time)

        if (charging == null || chargelevel == 100) {
            charging = chargeStatus.equals("Charging")
            insert(chargelevel, formatedDate, chargeStatus, context?.applicationContext)
        } else if (chargeStatus.equals("Charging") && !(charging as Boolean)) {
            charging = true
            insert(chargelevel, formatedDate, chargeStatus, context?.applicationContext)
        } else if (chargeStatus.equals("Discharging") && charging as Boolean) {
            charging = false
            insert(chargelevel, formatedDate, chargeStatus, context?.applicationContext)
        }
    }

    fun insert(chargelevel: Int, time: String, chargeStatus: String, context: Context?) {
        val bat = BatteryData(chargelevel, time, chargeStatus)
        var sqliteHelper:DatabaseHelper = DatabaseHelper(context!!)
        val status = sqliteHelper.insertData(bat)
        if(status>-1){
            Log.i("Charge Data added", "$bat")
        }else{
            Log.i("Charge not saved", "$bat")
        }

    }
}