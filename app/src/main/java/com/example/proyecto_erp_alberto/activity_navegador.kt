package com.example.proyecto_erp_alberto


import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class activity_navegador : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_navegador)

        val webView: WebView = findViewById(R.id.webview)
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true

        Toast.makeText(this, "Bienvenido al chatbot", Toast.LENGTH_SHORT).show()
        // Carga el archivo HTML desde la carpeta assets
        webView.loadUrl("file:///android_asset/index.html")
    }
}