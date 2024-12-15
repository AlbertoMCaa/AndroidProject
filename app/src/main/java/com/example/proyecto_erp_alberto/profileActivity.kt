package com.example.proyecto_erp_alberto

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto_erp_alberto.DatabaseHelper.DatabaseHelper
import kotlin.system.exitProcess

class profileActivity : AppCompatActivity() {

    private lateinit var speechToTextLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // checkboxes
        val darkModeCheckbox = findViewById<CheckBox>(R.id.darkModeCheckbox)
        val blueThemeCheckbox = findViewById<CheckBox>(R.id.blueThemeCheckbox)
        val greenThemeCheckbox = findViewById<CheckBox>(R.id.greenThemeCheckbox)

        val checkboxes = listOf(darkModeCheckbox, blueThemeCheckbox, greenThemeCheckbox)

        checkboxes.forEach { checkbox ->
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked)
                {
                    checkboxes.filter { it != checkbox }.forEach { it.isChecked = false }
                }
            }
        }

        val user = intent.getStringExtra("user")
        val userView = findViewById<TextView>(R.id.userName)
        userView.text = user

        // SpeechToText
        speechToTextLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK)
            {
                val data = result.data
                val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val recognizedText = results?.get(0) ?: "Texto no reconocido"

                processVoiceInput(recognizedText)
            }
        }

        val speechToTextButton = findViewById<Button>(R.id.speechToTextButton)
        speechToTextButton.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
            }
            speechToTextLauncher.launch(intent)
        }

        val saveButton = findViewById<Button>(R.id.saveButton)
        val cancelButton = findViewById<Button>(R.id.cancelButton)
        val resetButton = findViewById<Button>(R.id.resetButton)

        saveButton.setOnClickListener {
            Toast.makeText(this, "Función Guardar no implementada", Toast.LENGTH_SHORT).show()
        }

        cancelButton.setOnClickListener {
            Toast.makeText(this, "Función Cancelar no implementada", Toast.LENGTH_SHORT).show()
        }

        resetButton.setOnClickListener {
            Toast.makeText(this, "Función Resetear Configuración no implementada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processVoiceInput(input: String)
    {
        if (input.lowercase() == "chatbot")
        {
            val user = intent.getStringExtra("user")
            val intent = Intent(this,activity_navegador::class.java)
            intent.putExtra("user",user)
            startActivity(intent)
        }
        if (input.lowercase() == "añadir")
        {
            val user = intent.getStringExtra("user")
            val intent = Intent(this,addProject::class.java)
            intent.putExtra("user",user)
            startActivity(intent)
        }
        if (input.lowercase() == "salir")
        {
            Toast.makeText(this,"Saliendo...",Toast.LENGTH_SHORT).show()
            Thread.sleep(300)
            exitProcess(0)
        }
        if (input.lowercase() == "registrarse")
        {
            val user = intent.getStringExtra("user")
            val intent = Intent(this,registerActivity::class.java)
            intent.putExtra("user",user)
            startActivity(intent)
        }
        if(input.lowercase() == "activar tema oscuro")
        {
            findViewById<CheckBox>(R.id.darkModeCheckbox).isChecked = true
            Toast.makeText(this, "Tema oscuro activado", Toast.LENGTH_SHORT).show()
        }
        if(input.lowercase() == "activar tema azul")
        {
            findViewById<CheckBox>(R.id.blueThemeCheckbox).isChecked = true
            Toast.makeText(this, "Tema azul activado", Toast.LENGTH_SHORT).show()
        }
        if(input.lowercase() == "activar tema verde")
        {
            findViewById<CheckBox>(R.id.greenThemeCheckbox).isChecked = true
            Toast.makeText(this, "Tema verde activado", Toast.LENGTH_SHORT).show()
        }
        if(input.lowercase() == "mi correo")
        {
            val userView = findViewById<TextView>(R.id.userName)
            val user = userView.text.toString()

            val db = DatabaseHelper(this)
            db.getUserEmail(this, user)
        }
    }
}
