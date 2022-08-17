package com.example.rpn_calculator

import android.app.Activity
import android.content.Intent
import android.icu.math.BigDecimal
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.*


class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE = 10000
    private val maxHistorySize = 10

    private lateinit var stackView: ListView
    private lateinit var detector: GestureDetectorCompat
    private lateinit var screenView: LinearLayout

    private lateinit var adapter: ArrayAdapter<Double>

    private var precision = 2
    private var stackColor = "gray"
    private var backgroundColor = "white"

    private var stack = mutableListOf<Double>()
    private var stackHistory = mutableListOf<List<Double>>(stack)
    private var currentValueHistory = mutableListOf<String>("0")

    private var havePairForMathOperation: Boolean = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        detector = GestureDetectorCompat(this, MyGestureListener())

        screenView = findViewById(R.id.screen)
        stackView = findViewById(R.id.stack)

        adapter = ArrayAdapter(this, R.layout.aligned_right, stack)
        stackView.adapter = adapter
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.settings_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                intent.putExtra("precision", precision)
                intent.putExtra("stackColor", stackColor)
                intent.putExtra("backgroundColor", backgroundColor)

                startActivityForResult(intent, REQUEST_CODE)
            }
        }

        return super.onOptionsItemSelected(item)
    }




    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if (detector.onTouchEvent(event)) true
        else super.onTouchEvent(event)
    }



    inner class MyGestureListener: GestureDetector.SimpleOnGestureListener() {
        private val minimumSwipeLength = 10
        private val minimumSwipeSpeed = 10

        override fun onFling(
            downEvent: MotionEvent?,
            moveEvent: MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val xDifference = moveEvent?.x?.minus(downEvent!!.x) ?: 0.0F
            val yDifference = moveEvent?.y?.minus(downEvent!!.y) ?: 0.0F

            return if ( abs(xDifference) > abs(yDifference) && abs(xDifference) > minimumSwipeLength && abs(velocityX) > minimumSwipeSpeed && xDifference > 0)  {
                this@MainActivity.onRightSwipe()
                true
            }else super.onFling(downEvent, moveEvent, velocityX, velocityY)
        }
    }


    
    private fun onRightSwipe() {
        if (stackHistory.size > 1 && currentValueHistory.size > 1) {
            stackHistory.removeLast()
            stack.clear()
            stack.addAll(stackHistory.last())
            currentValueHistory.removeLast()
            currentValue.text = currentValueHistory.last()

            Toast.makeText(this, "Undo", Toast.LENGTH_SHORT).show()
            stackView.adapter = adapter
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Log.i("OnResult", "$requestCode, $resultCode, ${Activity.RESULT_OK}")
                val newPrecision = data.getIntExtra("precision", precision)
                var precisionReduced = false
                if (newPrecision < precision) precisionReduced = true
                precision = newPrecision
                stackColor = data.getStringExtra("stackColor") ?: stackColor
                backgroundColor = data.getStringExtra("backgroundColor") ?: backgroundColor

                if (precisionReduced){
                    for (i in 0 until stack.size) {
                        stack[i] = roundResult(stack[i])
                    }
                    for (i in 0 until stackHistory.size) {
                        val tmp = mutableListOf<Double>()
                        stackHistory[i].forEach {
                            tmp.add(roundResult(it))
                        }
                        stackHistory[i] = tmp
                    }
                }
                stackView.adapter = adapter


                screenView.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        resources.getIdentifier(backgroundColor, "color", packageName)
                    )
                )
                stackView.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        resources.getIdentifier(stackColor, "color", packageName)
                    )
                )
            }
        }
    }

    private fun roundResult(result: Double): Double{
        return result.toBigDecimal().setScale(precision, BigDecimal.ROUND_HALF_UP).toDouble()
    }


    private fun addToHistory(stack: MutableList<Double>, currentValue: String){
        if (stackHistory.size > maxHistorySize) {
            stackHistory.removeFirst()
            currentValueHistory.removeFirst()
        }
        stackHistory.add(stack.toList())
        currentValueHistory.add(currentValue)
    }


    fun numberAction(view: View) {

        if (view is Button){
            if (view.text.toString() == "."){
                if (currentValue.text.toString().indexOf(".") < 0 ) {
                    currentValue.append(view.text)
                }

            } else if (currentValue.text.toString() == "0"){
                if (view.text.toString() != "0")
                currentValue.text = view.text
            } else {
                currentValue.append(view.text)
            }
        }
    }



    fun specialOperationAction(view: View) {
        var didSomething = false

        if (view is Button){
            if (view.text.toString() == "DROP" && stack.size > 0) {
                stack.removeLast()
                didSomething = true

            } else if (view.text.toString()  == "SWAP"){
                if (stack.size >0){
                    val tmp = stack[stack.lastIndex]
                    stack[stack.lastIndex] =  roundResult(currentValue.text.toString().toDouble())
                    currentValue.text = tmp.toString()
                    didSomething = true
                }

            } else if (view.text.toString()  == "AC"){
                currentValue.text = "0"
                stack.clear()
                didSomething = true

            } else if (view.text.toString()  == "C"){
                if (currentValue.text.toString().length <= 1){
                    didSomething = currentValue.text.toString() != "0"
                    currentValue.text = "0"
                }else {
                    currentValue.text = currentValue.text.subSequence(0, currentValue.text.length - 1)
                    didSomething = true
                }

            } else if (view.text.toString()  == "+/-"){
                if (currentValue.text.toString() != "0" ){
                    if (currentValue.text.toString()[0] == '-'){
                        currentValue.text = currentValue.text.subSequence(1, currentValue.text.length)
                    } else {
                        currentValue.text = "-".plus(currentValue.text.toString())
                    }
                    didSomething = true
                }

            } else if (view.text.toString()  == "ENTER"){
                stack.add(roundResult(currentValue.text.toString().toDouble()))
                currentValue.text = "0"
                didSomething = true
            }

            if (didSomething){
                addToHistory(stack, currentValue.text.toString())
            }

            stackView.adapter = adapter
            havePairForMathOperation = stack.size > 0
        }
    }



    fun operationAction(view: View) {
        if (view is Button ) {
            val currentVal = currentValue.text.toString().toDouble()
            var didSomething = false

            if (view.text.toString() == "-" && havePairForMathOperation) {
                currentValue.text = (currentVal - stack[stack.lastIndex]).toString()
                stack.removeLast()
                didSomething = true
            } else if (view.text.toString() == "/" && havePairForMathOperation) {
                if (stack[stack.lastIndex] == 0.0){
                    Toast.makeText(this, "You cannot divide by 0", Toast.LENGTH_SHORT).show()
                } else{
                    val tmp = currentVal / stack[stack.lastIndex]
                    if (tmp == 0.0 || tmp == -0.0){
                        currentValue.text = "0"
                    } else {
                        currentValue.text = tmp.toString()
                    }
                    stack.removeLast()
                    didSomething = true
                }
            } else if (view.text.toString() == "SQRT"){
                currentValue.text = (sqrt(currentVal)).toString()
                didSomething = true
            } else if (view.text.toString() == "POW" && havePairForMathOperation){
                currentValue.text = (currentVal.pow(stack[stack.lastIndex])).toString()
                stack.removeLast()
                didSomething = true
            } else if (view.text.toString() == "*" && havePairForMathOperation){
                currentValue.text = (currentVal * stack[stack.lastIndex]).toString()
                stack.removeLast()
                didSomething = true
            } else if (view.text.toString() == "+" && havePairForMathOperation){
                currentValue.text = (currentVal + stack[stack.lastIndex]).toString()
                stack.removeLast()
                didSomething = true
            }

            if (didSomething){
                addToHistory(stack, currentValue.text.toString())
            }



            stackView.adapter = adapter
            havePairForMathOperation = stack.size > 0
        }
    }






}