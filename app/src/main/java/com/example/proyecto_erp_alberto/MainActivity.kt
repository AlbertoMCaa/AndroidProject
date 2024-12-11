package com.example.proyecto_erp_alberto

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.proyecto_erp_alberto.DatabaseHelper.DatabaseHelper
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener
{
    lateinit var buttonLogIn: Button
    lateinit var registerTextView: TextView
    lateinit var forgottenPassword: TextView
    lateinit var imageView: ImageView
    lateinit var editTexEmailAddress: EditText
    lateinit var editPassword: EditText

    //shake
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var lastShakeTime: Long = 0
    private val shakeThreshold = 35f

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        imageView = findViewById(R.id.imageView)
        imageView.setImageResource(R.drawable.logo_empresa)

        buttonLogIn = findViewById(R.id.buttonLogIn)
        registerTextView = findViewById(R.id.idRegistrarseActivity)
        forgottenPassword = findViewById(R.id.idOlvidasteLaContraseña)

        buttonLogIn.setOnClickListener{
            editTexEmailAddress = findViewById(R.id.editTexEmailAddress)
            editPassword = findViewById(R.id.editPassword)

            val email = editTexEmailAddress.text.toString().trim()
            val password = editPassword.text.toString().trim()

            val db = DatabaseHelper(this)

            if (db.loginUser(email, password))
            {
                val intent = Intent(this, splashActivity::class.java)
                intent.putExtra("user",db.getUsernameByEmail(email))
                startActivity(intent)
            }
            else
            {
                showAlertDialog()
            }
        }

        registerTextView.setOnClickListener {
            val intent = Intent(this, registerActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.slide_out_right, android.R.anim.slide_in_left)
        }

        forgottenPassword.setOnClickListener {
            val intent = Intent(this, forgotPassword::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onSensorChanged(event: SensorEvent?)
    {
        if (event != null)
        {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER)
            {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val acceleration = sqrt((x * x + y * y + z * z).toDouble())

                val currentTime = System.currentTimeMillis()
                if (acceleration > shakeThreshold && currentTime - lastShakeTime > 1000)
                {
                    lastShakeTime = currentTime
                    performLogin()
                }
            }
        }
    }

    private fun performLogin()
    {
        editTexEmailAddress = findViewById(R.id.editTexEmailAddress)
        editPassword = findViewById(R.id.editPassword)

        val email = editTexEmailAddress.text.toString().trim()
        val password = editPassword.text.toString().trim()

        val db = DatabaseHelper(this)

        if (db.loginUser(email, password))
        {
            val intent = Intent(this, splashActivity::class.java)
            intent.putExtra("user",db.getUsernameByEmail(email))
            startActivity(intent)
        }
        else
        {
            showAlertDialog()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume()
    {
        super.onResume()
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause()
    {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private fun showAlertDialog()
    {
        editPassword = findViewById(R.id.editPassword)

        val builder = AlertDialog.Builder(this)
            .setTitle("Credenciales Incorrectas")
            .setMessage("Correo o contraseña incorrectos.")
            .setPositiveButton("Aceptar") { dialog, which ->
                editPassword.text.clear()
            }
            .setCancelable(false)
            .setOnDismissListener {
                editPassword.text.clear()
            }

        val dialog = builder.create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.white))
    }
}
