package com.example.proyecto_erp_alberto.DatabaseHelper

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION)
{
    companion object
    {
        private const val DATABASE_NAME = "erp_database.db"
        private const val DATABASE_VERSION = 3

        private const val TABLE_USERS = "users"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_SALT = "salt"
        private const val COLUMN_PASSWORD_HASH = "password_hash"

        private const val TABLE_USER_TEXT_CODES = "user_text_codes"
        private const val COLUMN_COD_TEXT = "codTexto"
        private const val COLUMN_TIMESTAMP = "timestamp"

        private const val TABLE_PROJECTS = "projects"
        private const val COLUMN_ID = "id"
        private const val COLUMN_CONTRATISTA = "contratista"
        private const val COLUMN_PRESUPUESTO = "presupuesto"
        private const val COLUMN_TIEMPO_ESTIMADO = "tiempo_estimado"
        private const val COLUMN_LOCALIZACION = "localizacion"

        private const val CREATE_USERS_TABLE = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USERNAME TEXT PRIMARY KEY,
                $COLUMN_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_SALT TEXT NOT NULL,
                $COLUMN_PASSWORD_HASH TEXT NOT NULL
            )
        """

        private const val CREATE_USER_TEXT_CODES_TABLE = """
            CREATE TABLE $TABLE_USER_TEXT_CODES (
                $COLUMN_COD_TEXT INTEGER NOT NULL,
                $COLUMN_USERNAME TEXT NOT NULL,
                $COLUMN_TIMESTAMP INTEGER NOT NULL,
                PRIMARY KEY ($COLUMN_COD_TEXT, $COLUMN_USERNAME),
                FOREIGN KEY ($COLUMN_USERNAME) REFERENCES $TABLE_USERS($COLUMN_USERNAME)
                ON DELETE CASCADE ON UPDATE CASCADE
            )
        """

        private const val CREATE_PROJECTS_TABLE = """
            CREATE TABLE $TABLE_PROJECTS (
                $COLUMN_ID INTEGER NOT NULL,
                $COLUMN_USERNAME TEXT NOT NULL,
                $COLUMN_CONTRATISTA TEXT NOT NULL,
                $COLUMN_PRESUPUESTO REAL NOT NULL,
                $COLUMN_TIEMPO_ESTIMADO TEXT NOT NULL,
                $COLUMN_LOCALIZACION TEXT NOT NULL,
                PRIMARY KEY ($COLUMN_ID, $COLUMN_USERNAME),
                FOREIGN KEY ($COLUMN_USERNAME) REFERENCES $TABLE_USERS($COLUMN_USERNAME)
                ON DELETE CASCADE ON UPDATE CASCADE
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase)
    {
        db.execSQL(CREATE_USERS_TABLE)
        db.execSQL(CREATE_USER_TEXT_CODES_TABLE)
        db.execSQL(CREATE_PROJECTS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int)
    {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER_TEXT_CODES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROJECTS")
        onCreate(db)
    }

    private fun generateSalt(): String
    {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }

    private fun hashPasswordWithSalt(password: String, salt: String): String
    {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(Base64.getDecoder().decode(salt))
        val hash = md.digest(password.toByteArray())

        return hash.joinToString("") { "%02x".format(it) }
    }

    fun registerUser(username: String, email: String, password: String): Boolean
    {

        val salt = generateSalt()
        val hashedPassword = hashPasswordWithSalt(password, salt)

        val db = writableDatabase
        val sql = "INSERT INTO $TABLE_USERS ($COLUMN_USERNAME, $COLUMN_EMAIL, $COLUMN_SALT, $COLUMN_PASSWORD_HASH) VALUES (?, ?, ?, ?)"
        val statement = db.compileStatement(sql)

        return try
        {
            statement.bindString(1, username)
            statement.bindString(2, email)
            statement.bindString(3, salt)
            statement.bindString(4, hashedPassword)
            statement.executeInsert()
            true
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            false
        }
        finally
        {
            statement.close()
            db.close()
        }
    }
    fun updatePassword(username: String, newPassword: String): Boolean
    {
        val db = writableDatabase

        val salt = generateSalt()
        val hashedPassword = hashPasswordWithSalt(newPassword, salt)

        val sql = "UPDATE $TABLE_USERS SET $COLUMN_SALT = ?, $COLUMN_PASSWORD_HASH = ? WHERE $COLUMN_USERNAME = ?"

        val statement = db.compileStatement(sql)

        return try
        {
            statement.bindString(1, salt)
            statement.bindString(2, hashedPassword)
            statement.bindString(3, username)
            val rowsAffected = statement.executeUpdateDelete()

            // If at least one row was affected then it was a success
            rowsAffected > 0
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            false
        }
        finally
        {
            statement.close()
            db.close()
        }
    }


    fun loginUser(email: String, password: String): Boolean
    {
        val db = readableDatabase
        val sql = "SELECT $COLUMN_SALT, $COLUMN_PASSWORD_HASH FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ?"
        val cursor = db.rawQuery(sql, arrayOf(email))

        return try
        {
            if (cursor.moveToFirst())
            {
                val storedSalt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SALT))
                val storedHash = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD_HASH))
                val hashedPassword = hashPasswordWithSalt(password, storedSalt)
                storedHash == hashedPassword
            }
            else { false }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            false
        }
        finally
        {
            cursor.close()
            db.close()
        }
    }

    fun insertUserTextCode(codTexto: Int, username: String, timestamp: Long): Boolean
    {
        val db = writableDatabase
        val sql = "INSERT INTO $TABLE_USER_TEXT_CODES ($COLUMN_COD_TEXT, $COLUMN_USERNAME, $COLUMN_TIMESTAMP) VALUES (?, ?, ?)"
        val statement = db.compileStatement(sql)

        return try
        {
            statement.bindLong(1, codTexto.toLong())
            statement.bindString(2, username)
            statement.bindLong(3, timestamp)
            statement.executeInsert()
            true
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            false
        }
        finally
        {
            statement.close()
            db.close()
        }
    }

    fun getUsernameByEmail(email: String): String?
    {
        val db = readableDatabase
        val sql = "SELECT $COLUMN_USERNAME FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ?"
        val cursor = db.rawQuery(sql, arrayOf(email))

        return try
        {
            if (cursor.moveToFirst())
            {
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME))
            }
            else
            {
                null // No registers found
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            null
        }
        finally
        {
            cursor.close()
            db.close()
        }
    }
    fun getCodeIfValid(email: String): Int?
    {
        val db = readableDatabase
        val sql = """
        SELECT $COLUMN_COD_TEXT, $COLUMN_TIMESTAMP
        FROM $TABLE_USER_TEXT_CODES
        WHERE $COLUMN_USERNAME = (SELECT $COLUMN_USERNAME FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ?)
    """
        val cursor = db.rawQuery(sql, arrayOf(email))
        val currentTime = System.currentTimeMillis()

        return try
        {
            if (cursor.moveToFirst())
            {
                val code = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COD_TEXT))
                val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))

                if (currentTime - timestamp <= 5 * 60 * 1000) // 5 minutes in milliseconds
                {
                    code
                }
                else
                {
                    null // Expired code
                }
            }
            else
            {
                null // code not found
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            null
        }
        finally
        {
            cursor.close()
            db.close()
        }
    }

    fun deleteExpiredCodes()
    {
        val db = writableDatabase
        val currentTime = System.currentTimeMillis()
        val sql = "DELETE FROM $TABLE_USER_TEXT_CODES WHERE $COLUMN_TIMESTAMP < ?"
        val statement = db.compileStatement(sql)

        try
        {
            statement.bindLong(1, currentTime - 5 * 60 * 1000) // 5 minutes in milñiseconds
            statement.executeUpdateDelete()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        finally
        {
            statement.close()
            db.close()
        }
    }
    fun deleteAllCodes()
    {
        val db = writableDatabase
        val sql = "DELETE FROM $TABLE_USER_TEXT_CODES"

        try
        {
            db.execSQL(sql)
            println("Todos los códigos han sido eliminados.")
        }
        catch (e: Exception) { e.printStackTrace() }
        finally
        {
            db.close()
        }
    }

    fun insertProject(id: Int, username: String, contratista: String, presupuesto: Double, tiempoEstimado: String, localizacion: String): Boolean
    {
        val db = writableDatabase
        val sql = """
        INSERT INTO $TABLE_PROJECTS (
                $COLUMN_ID, $COLUMN_USERNAME, $COLUMN_CONTRATISTA, 
                $COLUMN_PRESUPUESTO, $COLUMN_TIEMPO_ESTIMADO, $COLUMN_LOCALIZACION
            ) VALUES (?, ?, ?, ?, ?, ?)
        """
        val statement = db.compileStatement(sql)

        return try
        {
            statement.bindLong(1, id.toLong())
            statement.bindString(2, username)
            statement.bindString(3, contratista)
            statement.bindDouble(4, presupuesto)
            statement.bindString(5, tiempoEstimado)
            statement.bindString(6, localizacion)

            statement.executeInsert()
            true
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            false
        }
        finally
        {
            statement.close()
            db.close()
        }
    }
    fun getProjectsByUser(username: String): List<Map<String, Any>>
    {
        val db = readableDatabase
        val sql = """
        SELECT $COLUMN_ID, $COLUMN_CONTRATISTA, $COLUMN_PRESUPUESTO, 
               $COLUMN_TIEMPO_ESTIMADO, $COLUMN_LOCALIZACION 
        FROM $TABLE_PROJECTS
        WHERE $COLUMN_USERNAME = ?
    """
        val cursor = db.rawQuery(sql, arrayOf(username))

        val projects = mutableListOf<Map<String, Any>>()

        return try
        {
            if (cursor.moveToFirst())
            {
                do
                {
                    val project = mapOf(
                        COLUMN_ID to cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        COLUMN_CONTRATISTA to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTRATISTA)),
                        COLUMN_PRESUPUESTO to cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRESUPUESTO)),
                        COLUMN_TIEMPO_ESTIMADO to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIEMPO_ESTIMADO)),
                        COLUMN_LOCALIZACION to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCALIZACION))
                    )
                    projects.add(project)
                } while (cursor.moveToNext())
            }
            projects
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            emptyList() // If an error occurs, return an empty list
        }
        finally
        {
            cursor.close()
            db.close()
        }
    }
    fun deleteProjectById(username: String, projectId: Int): Boolean
    { //Paso de seguir implementando. Hasta aquí llegó el proyecto.
        val db = writableDatabase
        val sql = """
        DELETE FROM $TABLE_PROJECTS
        WHERE $COLUMN_USERNAME = ? AND $COLUMN_ID = ?
    """
        val statement = db.compileStatement(sql)

        return try
        {
            statement.bindString(1, username)
            statement.bindLong(2, projectId.toLong())

            val rowsAffected = statement.executeUpdateDelete()

            rowsAffected > 0
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            false
        }
        finally
        {
            statement.close()
            db.close()
        }
    }

    fun getUserEmail(context: Context, username: String)
    {
        val db = readableDatabase
        val sql = "SELECT $COLUMN_EMAIL FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ?"
        val cursor = db.rawQuery(sql, arrayOf(username))

        try
        {
            if (cursor.moveToFirst())
            {
                val email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL))
                Toast.makeText(context, "Tu correo es: $email", Toast.LENGTH_LONG).show()
            }
            else
            {
                Toast.makeText(context, "Correo no encontrado.", Toast.LENGTH_SHORT).show()
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            Toast.makeText(context, "Error al obtener el correo.", Toast.LENGTH_SHORT).show()
        }
        finally
        {
            cursor.close()
            db.close()
        }
    }

}
