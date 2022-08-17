package com.example.rpn_calculator

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.NumberPicker
import android.widget.Spinner

class SettingsActivity : AppCompatActivity() {



    private var precision = 2
    private var stackColor = "gray"
    private var backgroundColor = "white"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        precision = intent.getIntExtra("precision",2)
        stackColor = intent.getStringExtra("stackColor") ?: "gray"
        backgroundColor = intent.getStringExtra("backgroundColor") ?: "white"


        val precisionPicker: NumberPicker = findViewById(R.id.precisionPicker)
        precisionPicker.wrapSelectorWheel = false
        precisionPicker.minValue = 0
        precisionPicker.maxValue = 10
        precisionPicker.value = precision

        precisionPicker.setOnValueChangedListener{
            _, _, newVal -> precision = newVal
        }

        val colors = arrayOf( "red", "green", "blue", "yellow", "orange" , "white", "gray", "black")

        val stackColorPicker: NumberPicker = findViewById(R.id.stackColorPicker)
        stackColorPicker.minValue = 0
        stackColorPicker.maxValue = colors.size - 1
        stackColorPicker.displayedValues = colors
        stackColorPicker.value = colors.indexOf(stackColor)

        stackColorPicker.setOnValueChangedListener{
            _, _, newVal -> stackColor = colors[newVal]
        }




        val backgroundColorPicker: NumberPicker = findViewById(R.id.backgroundColorPicker)
        backgroundColorPicker.minValue = 0
        backgroundColorPicker.maxValue = colors.size - 1
        backgroundColorPicker.displayedValues = colors
        backgroundColorPicker.value = colors.indexOf(backgroundColor)

        backgroundColorPicker.setOnValueChangedListener{
                _, _, newVal -> backgroundColor = colors[newVal]
        }
    }


    override fun finish() {
        val intent = Intent()
        intent.putExtra("precision", precision)
        intent.putExtra("stackColor", stackColor)
        intent.putExtra("backgroundColor", backgroundColor)
        setResult(Activity.RESULT_OK, intent)
        Log.i("SettingsActivity", "finish")
        super.finish()
    }



    fun backAction(view: View) {
        if (view is Button) {
            finish()
        }
    }

}