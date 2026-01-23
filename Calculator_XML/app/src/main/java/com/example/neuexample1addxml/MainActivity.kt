package com.example.neuexample1addxml
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //supportActionBar?.hide()   // 👈 Hides the title bar
        val num1 = findViewById<EditText>(R.id.etNum1)
        val num2 = findViewById<EditText>(R.id.etNum2)
        val button = findViewById<Button>(R.id.btnAdd)
        val result = findViewById<TextView>(R.id.tvResult)

        button.setOnClickListener {
            val n1 = num1.text.toString().toIntOrNull() ?: 0
            val n2 = num2.text.toString().toIntOrNull() ?: 0
            result.text = "Result: ${n1 + n2}"
        }
    }
}
