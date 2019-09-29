package com.will.wapm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.will.library.MethodDebug

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        add(1,1)
    }

    @MethodDebug
    fun add(a:Int,b:Int):Int{
        return a+b
    }
}
