package mx.edu.ittepic.ladm_u4_pu_asistencia_maestro_dacv

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.ittepic.ladm_u4_pu_asistencia_maestro_dacv.adapters.ListAdapter
import mx.edu.ittepic.ladm_u4_pu_asistencia_maestro_dacv.adapters.ListsViewAdapter
import mx.edu.ittepic.ladm_u4_pu_asistencia_maestro_dacv.databinding.ActivityListBinding
import mx.edu.ittepic.ladm_u4_pu_asistencia_maestro_dacv.models.List
import mx.edu.ittepic.ladm_u4_pu_asistencia_maestro_dacv.models.ListFile
import java.util.ArrayList

class ListActivity : AppCompatActivity() {

    lateinit var binding: ActivityListBinding
    lateinit var adapter : ListsViewAdapter
    lateinit var recyclerView: RecyclerView
    val arrayList = ArrayList<ListFile>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.rvList

        obtenerListas()

        adapter = ListsViewAdapter(arrayList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter


    }

    private fun obtenerListas() {
        FirebaseFirestore.getInstance()
            .collection("listas")
            .get()
            .addOnSuccessListener { results->
                arrayList.clear()
                for (doc in results){
                    val list = ListFile()
                    list.grupo = doc.getString("grupo").toString()
                    list.dia = doc.getString("dia").toString()
                    arrayList.add(list)
                }
                adapter.notifyDataSetChanged()
            }
    }
}