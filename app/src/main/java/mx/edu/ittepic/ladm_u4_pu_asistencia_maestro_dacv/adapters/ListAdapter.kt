package mx.edu.ittepic.ladm_u4_pu_asistencia_maestro_dacv.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.edu.ittepic.ladm_u4_pu_asistencia_maestro_dacv.R
import mx.edu.ittepic.ladm_u4_pu_asistencia_maestro_dacv.models.List

class ListAdapter (private val list:ArrayList<List>): RecyclerView.Adapter<ListAdapter.ViewHolder>() {
    /************************************
     * DANIEL ALEJANDRO CALDERÓN VIGREN *
     ************************************/

    inner class  ViewHolder(item: View) :RecyclerView.ViewHolder(item){
        var noControl : TextView = item.findViewById(R.id.no_control)
        var hour : TextView = item.findViewById(R.id.hour_list)
        var date : TextView = item.findViewById(R.id.date_list)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_view_list,parent,false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.noControl.setText(list[position].noControl)
        holder.date.setText(list[position].date)
        holder.hour.setText(list[position].hour)
    }

    override fun getItemCount(): Int {
        return list.size
    }
}