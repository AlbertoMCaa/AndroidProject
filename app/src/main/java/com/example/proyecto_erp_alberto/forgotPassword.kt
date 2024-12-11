package com.example.proyecto_erp_alberto

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto_erp_alberto.DatabaseHelper.DatabaseHelper
import com.example.proyecto_erp_alberto.email.EmailSender
import com.google.android.material.snackbar.Snackbar

class forgotPassword : AppCompatActivity()
{
    lateinit var sendEmail: Button
    lateinit var verifyCode: Button
    lateinit var submitPassword: Button

    lateinit var email: EditText

    lateinit var emailTextView: TextView
    lateinit var passwordLabel: TextView
    lateinit var newPassword: EditText
    lateinit var confirmNewPassword: EditText
    lateinit var textCode: EditText

    private lateinit var passwordStrength1: TextView
    private lateinit var passwordStrength2: TextView

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)

        sendEmail = findViewById(R.id.buttonSendEmail)
        verifyCode = findViewById(R.id.buttonVerifyCode)
        submitPassword = findViewById(R.id.buttonSubmitNewPassword)

        //First stage
        email = findViewById(R.id.editTextEmailForgottenPassword)

        //SecondStage
        passwordLabel = findViewById(R.id.textViewNewPasswordLabel)
        emailTextView = findViewById(R.id.textViewEnteredEmail)
        textCode = findViewById(R.id.editTextCode)

        //Third Stage

        passwordStrength1 = findViewById(R.id.passwordStrengthText2)
        passwordStrength2 = findViewById(R.id.passwordStrengthText3)

        newPassword = findViewById(R.id.editTextNewPassword)
        confirmNewPassword = findViewById(R.id.editTextConfirmNewPassword)

        newPassword.addTextChangedListener(object : TextWatcher
        {
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

            override fun afterTextChanged(editable: Editable?)
            {
                val password = editable.toString()
                val strength = getPasswordStrength(password)
                passwordStrength1.text = strength
                passwordStrength1.visibility =
                    if (password.isEmpty()) View.GONE else View.VISIBLE
            }
        })
        confirmNewPassword.addTextChangedListener(object : TextWatcher
        {
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

            override fun afterTextChanged(editable: Editable?)
            {
                val password = editable.toString()
                val strength = getPasswordStrength(password)
                passwordStrength2.text = strength
                passwordStrength2.visibility =
                    if (password.isEmpty()) View.GONE else View.VISIBLE
            }
        })

        sendEmail.setOnClickListener {
            if (email.text.isEmpty())
            {
                email.error = "Campo requerido"
                Toast.makeText(this,"Campo requerido",Toast.LENGTH_SHORT).show()
            }
            else if (isValidEmail(email.text.toString().trim()))
            {
                val db = DatabaseHelper(this)
                val bool = db.getUsernameByEmail(email.text.toString().trim()) != null // user's email found and thus, the email exists.
                val user = db.getUsernameByEmail(email.text.toString().trim())

                if (bool)
                {
                    snackBarEmailSent()
                    val code = EmailSender.sendRecoveryEmail(this,email.text.toString().trim())
                    db.deleteAllCodes() // Debug purposes. I cannot wait 5 minutes rn
                    db.insertUserTextCode(code, user.toString(),System.currentTimeMillis())

                    fadeOutWithTranslation(email)
                    {
                        fadeOutWithTranslation(sendEmail)
                        {
                            emailTextView.text = "Correo ingresado: ${email.text}"
                            fadeInWithTranslation(emailTextView)
                            fadeInWithTranslation(textCode)
                            fadeInWithTranslation(verifyCode)
                        }
                    }
                }
                else
                {
                    Toast.makeText(this, "El correo electrónico ingresado no existe", Toast.LENGTH_SHORT).show()
                }
            }
            else
            {
                email.error = "El correo ingresado no es un correo electrónico válido"
                Toast.makeText(this,"El correo ingresado no es un correo electrónico válido",Toast.LENGTH_SHORT).show()
            }
        }

        verifyCode.setOnClickListener {
            val db = DatabaseHelper(this)
            val user = db.getUsernameByEmail(email.text.toString())
            val temp = if (!textCode.text.isNullOrEmpty()) textCode.text.toString().toInt() else 0 // if you remove this line, a crash will happen if you leave it blank
            val equal = temp == db.getCodeIfValid(email.text.toString().trim())  // If the code submitted by the user equals the one in the database, then you may proceed.
            if (equal)
            {
                    textCode.visibility = View.GONE
                    verifyCode.visibility = View.GONE
                    passwordLabel.text = "Usuario: $user"
                    passwordLabel.visibility = View.VISIBLE
                    newPassword.visibility = View.VISIBLE
                    confirmNewPassword.visibility = View.VISIBLE
                    submitPassword.visibility = View.VISIBLE
            }
            else
            {
                Toast.makeText(this,"Código incorrecto. Vuelva a intentarlo",Toast.LENGTH_SHORT).show()
                snackBarInvalidCode()

            }
        }

        submitPassword.setOnClickListener()
        {
            val password = newPassword.text.toString()
            val confirmPassword = confirmNewPassword.text.toString()

            if (password.isEmpty() || confirmPassword.isEmpty())
            {
                newPassword.error = "Este campo es obligatorio"
                confirmNewPassword.error = "Este campo es obligatorio"
            }
            else
            {
                if (password != confirmPassword)
                {
                    newPassword.error = "Las contraseñas no coinciden"
                }
                else if (!isPasswordStrong(password))
                {
                    newPassword.error = "La contraseña no es lo suficientemente fuerte"
                }
                else
                {
                    val db = DatabaseHelper(this)
                    val user = db.getUsernameByEmail(email.text.toString())
                    if (user != null)
                    {
                        db.updatePassword(user,password)
                    }
                    Toast.makeText(this,"Contraseña actualizada",Toast.LENGTH_SHORT).show()
                    snackBarUpdatedPassword()
                    Thread.sleep(450)
                    finish()
                }
            }
        }
    }
    fun isValidEmail(email: String): Boolean
    {
        val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex()
        return emailRegex.matches(email)
    }
    private fun snackBarEmailSent()
    {
        Snackbar.make(findViewById(android.R.id.content), "Código de verificación enviado. Revise su correo electrónico",
            Snackbar.LENGTH_LONG)
            .setAction("OK")
            {
                //Nothing
            }
            .setActionTextColor(resources.getColor(android.R.color.holo_red_light))
            .show()
    }
    private fun snackBarInvalidCode()
    {
        Snackbar.make(findViewById(android.R.id.content), "Código incorrecto. Vuelva a intentarlo",
            Snackbar.LENGTH_LONG)
            .setAction("OK")
            {
                //Nothing
            }
            .setActionTextColor(resources.getColor(android.R.color.holo_red_light))
            .show()
    }
    private fun snackBarUpdatedPassword()
    {
        Snackbar.make(findViewById(android.R.id.content), "Contraseña actualizada.",
            Snackbar.LENGTH_LONG)
            .setAction("OK")
            {
                //Nothing
            }
            .setActionTextColor(resources.getColor(android.R.color.holo_red_light))
            .show()
    }

    private fun fadeOutWithTranslation(view: View, duration: Long = 300, endAction: () -> Unit = {})
    {
        view.animate()
            .alpha(0f) // fading
            .translationY(50f) // downside movement
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                view.visibility = View.GONE
                endAction()
            }
            .start()
    }

    private fun fadeInWithTranslation(view: View, duration: Long = 300, startAction: () -> Unit = {})
    {
        view.alpha = 0f
        view.translationY = 50f // slightly downside
        view.visibility = View.VISIBLE
        view.animate()
            .alpha(1f) // increases opacity
            .translationY(0f) // back to it's original position
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator()) // smooth interpolation
            .withStartAction(startAction)
            .start()
    }


    //Helper functions
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

}