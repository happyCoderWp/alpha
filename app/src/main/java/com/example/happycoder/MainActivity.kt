package com.example.happycoder

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import com.example.happycoder.livedatabus.LivedataBus

class MainActivity : AppCompatActivity() {

  private val TAG = "MainActivity"

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    LivedataBus.toObservable(Event::class.java)?.observe(this, Observer {
      Log.e(TAG, "receive info from event , name is " + it.name)
    })
    LivedataBus.post(Event("test liveData bus"))
  }
}
