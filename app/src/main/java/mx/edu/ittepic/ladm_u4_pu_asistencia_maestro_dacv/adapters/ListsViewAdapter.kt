package mx.edu.ittepic.ladm_u4_pu_asistencia_maestro_dacv.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.edu.ittepic.ladm_u4_pu_asistencia_maestro_dacv.R
import mx.edu.ittepic.ladm_u4_pu_asistencia_maestro_dacv.models.ListFile

class ListsViewAdapter(private val list:ArrayList<ListFile>): RecyclerView.Adapter<ListsViewAdapter.ViewHolder>() {
    inner class ViewHolder(item:View): RecyclerView.ViewHolder(item) {
        val grupo: TextView = item.findViewById(R.id.grupo)
        val dia: TextView = item.findViewById(R.id.dia)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.content_main,parent,false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.grupo.setText(list[position].grupo)
        holder.dia.setText(list[position].dia)
    }

    override fun getItemCount(): Int {
       return list.size
    }
}