package com.example.proyecto_erp_alberto

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_erp_alberto.DatabaseHelper.DatabaseHelper

class erpPrincipalActivity : AppCompatActivity()
{
    private lateinit var recyclerView: RecyclerView

    private lateinit var hamburger: ImageView
    private lateinit var profile: ImageView

    private lateinit var add: Button

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_erp_principal)

        recyclerView = findViewById(R.id.worksRecyclerView)

        val user = intent.getStringExtra("user")
        Toast.makeText(this,"Bienvenido $user",Toast.LENGTH_SHORT).show()

        val dbHelper = DatabaseHelper(this)
        val projectsList = user?.let {
            dbHelper.getProjectsByUser(it).map { projectMap ->
                Project(
                    id = projectMap["id"] as Int,
                    contratista = projectMap["contratista"] as String,
                    presupuesto = projectMap["presupuesto"] as Double,
                    tiempoEstimado = projectMap["tiempo_estimado"] as String,
                    localizacion = projectMap["localizacion"] as String
                )
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = projectsList?.let { ProjectsAdapter(it) }

        hamburger = findViewById(R.id.hamburgerIcon)
        profile = findViewById(R.id.profileIcon)

        hamburger.setOnClickListener()
        {
            val intent = Intent(this,activity_navegador::class.java)
            startActivity(intent)
        }
        profile.setOnClickListener()
        {
            val intent = Intent(this,profileActivity::class.java)
            intent.putExtra("user",user)
            startActivity(intent)
        }

        add = findViewById(R.id.addButton)
        add.setOnClickListener()
        {
            val intent = Intent(this,addProject::class.java)
            intent.putExtra("user",user)
            startActivity(intent)
        }
    }

    override fun onResume()
    {
        super.onResume()
        val updatedProjectsList = intent.getStringExtra("user")?.let {
            DatabaseHelper(this).getProjectsByUser(it).map { projectMap ->
                Project(
                    id = projectMap["id"] as Int,
                    contratista = projectMap["contratista"] as String,
                    presupuesto = projectMap["presupuesto"] as Double,
                    tiempoEstimado = projectMap["tiempo_estimado"] as String,
                    localizacion = projectMap["localizacion"] as String
                )
            }
        }
        recyclerView.adapter = updatedProjectsList?.let { ProjectsAdapter(it) }
    }
}



data class Project(
    val id: Int,
    val contratista: String,
    val presupuesto: Double,
    val tiempoEstimado: String,
    val localizacion: String
)

class ProjectsAdapter(private val projects: List<Project>) :
    RecyclerView.Adapter<ProjectsAdapter.ProjectViewHolder>() {

    class ProjectViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val contratista: TextView = view.findViewById(R.id.tvContratista)
        val presupuesto: TextView = view.findViewById(R.id.tvPresupuesto)
        val tiempoEstimado: TextView = view.findViewById(R.id.tvTiempoEstimado)
        val localizacion: TextView = view.findViewById(R.id.tvLocalizacion)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.project_item_view, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = projects[position]
        holder.contratista.text = project.contratista
        holder.presupuesto.text = "Presupuesto: ${project.presupuesto}€"
        holder.tiempoEstimado.text = "Tiempo: ${project.tiempoEstimado}"
        holder.localizacion.text = "Ubicación: ${project.localizacion}"
    }

    override fun getItemCount() = projects.size
}

