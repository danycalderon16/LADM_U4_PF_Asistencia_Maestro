package mx.edu.ittepic.ladm_u4_pu_asistencia_maestro_dacv.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.edu.ittepic.ladm_u4_pu_asistencia_maestro_dacv.R
import mx.edu.ittepic.ladm_u4_pu_asistencia_maestro_dacv.models.ListFile

class ListsViewAdapter(private val list:ArrayList<ListFile>, itemListener: onItemClickListenr): RecyclerView.Adapter<ListsViewAdapter.ViewHolder>() {

    var mListener : onItemClickListenr = itemListener
    val VIEWER = 322
    val DOWNLOAD = 323

    inner class ViewHolder(item:View): RecyclerView.ViewHolder(item) {
        val grupo: TextView = item.findViewById(R.id.grupo)
        val dia: TextView = item.findViewById(R.id.dia)
        val download: ImageView = item.findViewById(R.id.download)
        val viewer: ImageView = item.findViewById(R.id.viewer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.content_main,parent,false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.grupo.setText(list[position].grupo)
        holder.dia.setText(list[position].dia)
        holder.viewer.setOnClickListener {
            mListener.onItemClick(list[position],position,VIEWER)
        }
        holder.download.setOnClickListener {
            mListener.onItemClick(list[position],position,DOWNLOAD)
        }
    }

    override fun getItemCount(): Int {
       return list.size
    }

    interface onItemClickListenr{
        fun onItemClick(list: ListFile, i:Int,tipo:Int)
    }
}