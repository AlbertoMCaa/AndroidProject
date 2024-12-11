package com.example.proyecto_erp_alberto

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto_erp_alberto.DatabaseHelper.DatabaseHelper
import com.example.proyecto_erp_alberto.email.EmailSender
import com.google.android.material.snackbar.Snackbar

class registerActivity : AppCompatActivity()
{
    //Environment variables
    private var backPressedFlag = false

    //XML variables
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var passwordStrengthText: TextView
    private lateinit var editTextUsername: EditText
    private lateinit var buttonRegister: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)
        editTextUsername = findViewById(R.id.editTextUsername)
        buttonRegister = findViewById(R.id.buttonRegister)
        passwordStrengthText = findViewById(R.id.passwordStrengthText)


        //Listeners
        editTextPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence?,
                start: Int,
                before: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence?,
                start: Int,
                before: Int,
                after: Int
            ) {
            }

            override fun afterTextChanged(editable: Editable?) {
                val password = editable.toString()
                val strength = getPasswordStrength(password)
                passwordStrengthText.text = strength
                passwordStrengthText.visibility =
                    if (password.isEmpty()) View.GONE else View.VISIBLE
            }
        })

        onBackPressedDispatcher.addCallback(this) {
            if (hasTextInFields()) {
                if (backPressedFlag)
                {
                    finish() // if enabled, it will finish the current activity, going back.
                }
                else
                {
                    showExitConfirmationDialog() // if theres text, show a confirmation dialog.
                    onBackPressed()
                }
            }
            else
            {
                finish() // if there is no text continue.
            }
        }

        buttonRegister.setOnClickListener {
            if (areFieldsValid())
            {
                val email = editTextEmail.text.toString().trim()
                val user = editTextUsername.text.toString().trim()
                val password = editTextPassword.text.toString().trim()

                val db = DatabaseHelper(this)
                if (db.registerUser(user,email, password))
                {
                    Toast.makeText(this,"Usuario $user fue registrado con éxito.",Toast.LENGTH_SHORT).show()
                    Thread.sleep(300)
                    finish()
                }
                else
                {
                    showExitConfirmationSnackbar()
                }
            }
        }
    }

    private fun areFieldsValid(): Boolean
    {
        var isValid = true

        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()
        val confirmPassword = editTextConfirmPassword.text.toString().trim()
        val userName = editTextUsername.text.toString().trim()

        if (editTextEmail.text.isEmpty())
        {
            editTextEmail.error = "Este campo es obligatorio"
            isValid = false
        }
        else if(!isValidEmail(email))
        {
            editTextEmail.error = "El correo ingresado no es un correo electrónico válido"
            Toast.makeText(this,"El correo ingresado no es un correo electrónico válido",Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (editTextUsername.text.isNullOrEmpty())
        {
            editTextUsername.error = "Este campo es obligatorio"
            isValid = false
        }

        if (password.isEmpty() || confirmPassword.isEmpty())
        {
            editTextPassword.error = "Este campo es obligatorio"
            editTextConfirmPassword.error = "Este campo es obligatorio"
            isValid = false
        }
        else
        {
            if (password != confirmPassword)
            {
                editTextConfirmPassword.error = "Las contraseñas no coinciden"
                isValid = false
            }
            else if (!isPasswordStrong(password))
            {
                editTextPassword.error = "La contraseña no es lo suficientemente fuerte"
                isValid = false
            }
        }
        return isValid
    }

    private fun isPasswordStrong(password: String): Boolean
    {
        val minLength = 8
        val hasUppercase = password.any { it.isUpperCase() }
        val hasLowercase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { "!@#$%^&*()_-".contains(it) }

        return password.length >= minLength && hasUppercase && hasLowercase && hasDigit && hasSpecialChar
    }

    private fun getPasswordStrength(password: String): String
    {
        val minLength = 8
        val hasUppercase = password.any { it.isUpperCase() }
        val hasLowercase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { "!@#$%^&*()_-".contains(it) }

        return when
        {
            password.length < minLength -> "Muy débil (mínimo 8 caracteres)"
            !hasUppercase || !hasLowercase || !hasDigit || !hasSpecialChar -> "Débil: Agrega mayúsculas, minúsculas, números y/o caracteres especiales"
            password.length >= minLength && hasUppercase && hasLowercase && hasDigit && hasSpecialChar -> "Fuerte"
            else -> "Moderada"
        }
    }

    private fun hasTextInFields(): Boolean
    {
        return editTextEmail.text.isNotEmpty() || editTextPassword.text.isNotEmpty() ||
                editTextUsername.text.isNotEmpty() || editTextConfirmPassword.text.isNotEmpty()
    }
    private fun showExitConfirmationDialog()
    {
        val dialog = AlertDialog.Builder(this)
            .setMessage("¿Estás seguro de que quieres salir? Los datos ingresados se perderán.")
            .setCancelable(false)
            .setPositiveButton("Sí") { dialogInterface, _ ->
                backPressedFlag = true
                finish()
                dialogInterface.dismiss()
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
    }
    private fun showExitConfirmationSnackbar()
    {
        Snackbar.make(findViewById(android.R.id.content), "El nombre de usuario o el correo electrónico ya están registrados. Por favor, elige otro.",
            Snackbar.LENGTH_LONG)
            .setAction("OK")
            {
                //Nothing
            }
            .setActionTextColor(resources.getColor(android.R.color.holo_red_light))
            .show()
    }
    fun isValidEmail(email: String): Boolean
    {
        val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex()
        return emailRegex.matches(email)
    }
}
