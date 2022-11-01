package com.example.battery_report_android

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory.Companion.instance
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private var batteryReceiver = BatteryBroadcastReceiver()
    private var timeReceiver = TimeBroadcastReceiver()
    private lateinit var sqliteHelper:DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sqliteHelper = DatabaseHelper(applicationContext)

        btnStart.setOnClickListener {
            if(btnStart.text.equals("Start")){
                this.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

                this.registerReceiver(timeReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
                btnStart.text = "Stop"
                Toast.makeText(this, "Battery data recording started", Toast.LENGTH_SHORT).show()
            }else{
                insertLastData()
                this.unregisterReceiver(batteryReceiver)
                this.unregisterReceiver(timeReceiver)
                btnStart.text = "Start"
                Toast.makeText(this, "Battery data recording stopped", Toast.LENGTH_SHORT).show()
            }
        }
        btnReport.setOnClickListener {
            getBatteryData()
        }
    }
    private fun getBatteryData(){
        var dataList = sqliteHelper.getAllData()
        processData(dataList)
//        Log.d("output", dataList.toString())
    }
    private fun processData(list: ArrayList<BatteryData>){
        var badCount = 0; var optimalCount=0; var spotCount=0

        var previousChargePercent = 0; var percent = 0
        var previousTime: Date = Date(); var time: Date = Date(); var centStart: Date = Date()
        var previousChargeStatus = ""; var chargeStatus = ""
        var centStartFlag = false

        var dischargeTime: Double = 0.0
        var dischargePercent = 0


        val dateFormat = SimpleDateFormat(
            "HH:mm:ss"
        )

        var hourReport:ArrayList<String> = ArrayList<String>()
        for ((i, curData) in list.withIndex()) {
            if(i==0){
                previousChargePercent = curData.batteryPercent
                previousTime = dateFormat.parse(curData.curTime)
                previousChargeStatus = curData.chargeStatus

                previousChargePercent == 100
                continue;
            }
            percent = curData.batteryPercent
            time = dateFormat.parse(curData.curTime)
            chargeStatus = curData.chargeStatus

            if (!centStartFlag && percent == 100 && chargeStatus.equals("Charging"))
            {
                centStart = time
                centStartFlag = true
            }
            else if (percent == 100 && (chargeStatus.equals("Discharging") || (chargeStatus.equals("Charging") && (i == list.size - 1))))
            {
                var diff = time.time - centStart.time
                if ((diff/60000) >= 30)
                    badCount += 1;
                else
                    optimalCount += 1
                centStartFlag = false
            }
            else if (chargeStatus.equals("Discharging") && previousChargeStatus.equals("Charging") && percent < 100)
            {
                spotCount += 1
            }

            if (chargeStatus.equals("Charging") && previousChargeStatus.equals("Discharging"))
            {
                dischargeTime += (time.time - previousTime.time)/60000
                dischargePercent += previousChargePercent - percent
            }
            if (time.toString().contains(":00:00") || (i == list.size - 1))
            {
                if (chargeStatus.equals("Discharging") && previousChargeStatus.equals("Discharging"))
                {
                    if (time.toString().contains(":00:00")) {
                        dischargeTime += (60 - previousTime.minutes)
                    }else
                        dischargeTime += (time.time - previousTime.time)/60000
                    dischargePercent += previousChargePercent - percent
                }
                var curReport = "$dischargePercent discharged in $dischargeTime minutes"
                hourReport.add(curReport)
                dischargeTime = 0.0
                dischargePercent = 0
            }
            previousChargePercent = percent
            previousTime = time
            previousChargeStatus = chargeStatus
        }
        var hourlyReport = hourReport.joinToString(separator = "\n")
        hourlyReport = hourlyReport + "\nBad: $badCount, Optimal: $optimalCount, Spot: $spotCount"
        debugText.text = hourlyReport
    }

    private fun insertLastData(){
        val time = Calendar.getInstance().time
        val formatter = SimpleDateFormat("hh:mm:ss") //or use getDateInstance()
        val formatedTime = formatter.format(time)
        val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = this?.registerReceiver(null, iFilter)

        var chargelevel = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
        var status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        var chargeStatus: String
        if(status == BatteryManager.BATTERY_STATUS_CHARGING){
            chargeStatus = "Charging"
        }else{
            chargeStatus = "Discharging"
        }
        val bat = BatteryData(chargelevel,formatedTime,chargeStatus)
        Log.d("Click", "$bat ")
    }
}