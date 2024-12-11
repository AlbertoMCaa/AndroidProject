package com.example.proyecto_erp_alberto

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.proyecto_erp_alberto.DatabaseHelper.DatabaseHelper
import com.google.android.material.internal.EdgeToEdgeUtils

class addProject : AppCompatActivity()
{
    private lateinit var save: Button

    lateinit var idEdit: EditText
    lateinit var contratistaEdit: EditText
    lateinit var presupuestoEdit: EditText
    lateinit var tiempoEdit: EditText
    lateinit var localizacionEdit: EditText
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_project)

        idEdit = findViewById(R.id.idEditText)
        contratistaEdit = findViewById(R.id.contratistaEditText)
        presupuestoEdit = findViewById(R.id.presupuestoEditText)
        tiempoEdit = findViewById(R.id.tiempoEditText)
        localizacionEdit = findViewById(R.id.localizacionEditText)


        save = findViewById(R.id.saveButton)
        save.isClickable = false

        idEdit.addTextChangedListener(inputTextWatcher)
        contratistaEdit.addTextChangedListener(inputTextWatcher)
        presupuestoEdit.addTextChangedListener(inputTextWatcher)
        tiempoEdit.addTextChangedListener(inputTextWatcher)
        localizacionEdit.addTextChangedListener(inputTextWatcher)

        save.setOnClickListener {
            if (isValid())
            {
                try {
                    val id = idEdit.text.toString().toInt()
                    val contratista = contratistaEdit.text.toString()
                    val presupuesto = presupuestoEdit.text.toString().toDouble()
                    val tiempoEstimado = tiempoEdit.text.toString()
                    val localizacion = localizacionEdit.text.toString()

                    val user = intent.getStringExtra("user")
                    if (user != null)
                    {
                        val db = DatabaseHelper(this)
                        val success = db.insertProject(id, user, contratista, presupuesto, tiempoEstimado, localizacion)
                        if (success)
                        {
                            Toast.makeText(this, "Proyecto añadido correctamente.", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        else
                        {
                            Toast.makeText(this, "Error al añadir el proyecto.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else
                    {
                        Toast.makeText(this, "Usuario no válido.", Toast.LENGTH_SHORT).show()
                    }
                }
                catch (e: Exception)
                {
                    Toast.makeText(this, "Error al convertir los valores.", Toast.LENGTH_SHORT).show()
                }
            } else
            {
                Toast.makeText(this, "Por favor, completa todos los campos correctamente.", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun isValid(): Boolean
    {
        var isValid = true

        if (idEdit.text.isNullOrEmpty())
        {
            idEdit.error = "No puede estar vacío"
            isValid = false
        }
        else if (!idEdit.text.isDigitsOnly())
        {
            idEdit.error = "Solo se permiten números enteros"
            isValid = false
        }

        if (presupuestoEdit.text.isNullOrEmpty())
        {
            presupuestoEdit.error = "No puede estar vacío"
            isValid = false
        }
        else if (!presupuestoEdit.text.matches(Regex("^[0-9]+(\\.[0-9]+)?$")))
        {
            presupuestoEdit.error = "Solo se admiten números (decimales opcionales)"
            isValid = false
        }

        if (contratistaEdit.text.isNullOrEmpty())
        {
            contratistaEdit.error = "No puede estar vacío"
            isValid = false
        }

        if (tiempoEdit.text.isNullOrEmpty())
        {
            tiempoEdit.error = "No puede estar vacío"
            isValid = false
        }

        if (localizacionEdit.text.isNullOrEmpty())
        {
            localizacionEdit.error = "No puede estar vacío"
            isValid = false
        }

        save.isClickable = isValid

        return isValid
    }
    private val inputTextWatcher = object : TextWatcher
    {
        override fun beforeTextChanged(charSequence: CharSequence?, start: Int, before: Int, after: Int)
        {
            save.isClickable = false
        }

        override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, after: Int) {}

        override fun afterTextChanged(editable: Editable?)
        {
            isValid()
        }
    }
}