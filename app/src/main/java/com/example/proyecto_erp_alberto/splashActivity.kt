package com.example.proyecto_erp_alberto

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class splashActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            val user = intent.getStringExtra("user")
            val intent = Intent(this, erpPrincipalActivity::class.java)
            intent.putExtra("user",user)
            startActivity(intent)
            finish() // !
        }, 2000) //2000
    }
}