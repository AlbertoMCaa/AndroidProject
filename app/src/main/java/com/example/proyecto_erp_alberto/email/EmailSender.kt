package com.example.proyecto_erp_alberto.email

import android.content.Context
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailSender(private val senderEmail: String, private val senderPassword: String)
{
    companion object
    {
        fun generateRecoveryCode(): String
        {
            return (10000..99999).random().toString()
        }

        fun sendRecoveryEmail(context: Context, userEmail: String): Int
        {
            var isSent:Boolean = false
            val senderEmail = "dam72856@gmail.com"
            val senderPassword = "qgob vfpz enrt pgze"

            val recoveryCode = generateRecoveryCode()
            val subject = "Código de recuperación de cuenta"
            val body = """
                Tu código de recuperación es: $recoveryCode.
                
                Por razones de seguridad, este código tiene una validez de 5 minutos.
            """.trimIndent()

            Thread {
                val emailSender = EmailSender(senderEmail, senderPassword)
                isSent = emailSender.sendEmail(userEmail, subject, body)

                if (isSent) {//curl -X POST http://192.168.1.61:8000/generate -H "Content-Type: application/json" -d '{"input_text": "hola"}'
                    println("Correo enviado exitosamente")
                }
                else
                {
                    println("Error al enviar el correo")
                }
            }.start()
            return recoveryCode.toInt()
        }
    }

    fun sendEmail(recipient: String, subject: String, body: String): Boolean
    {
        val properties = Properties()

        properties["mail.smtp.host"] = "smtp.gmail.com"
        properties["mail.smtp.port"] = "587"
        properties["mail.smtp.auth"] = "true"
        properties["mail.smtp.starttls.enable"] = "true"

        val session = Session.getInstance(properties, object : Authenticator()
        {
            override fun getPasswordAuthentication(): PasswordAuthentication
            {
                return PasswordAuthentication(senderEmail, senderPassword)
            }
        })

        return try
        {
            val message = MimeMessage(session) //create the email
            message.setFrom(InternetAddress(senderEmail))
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient))
            message.subject = subject
            message.setText(body)

            Transport.send(message) //send the email
            true
        }
        catch (e: MessagingException)
        {
            e.printStackTrace()
            false
        }
    }
}
