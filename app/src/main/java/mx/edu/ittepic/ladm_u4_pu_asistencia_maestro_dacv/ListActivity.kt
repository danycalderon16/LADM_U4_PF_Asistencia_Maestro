package mx.edu.ittepic.ladm_u4_pu_asistencia_maestro_dacv

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.ittepic.ladm_u4_pu_asistencia_maestro_dacv.adapters.ListsViewAdapter
import mx.edu.ittepic.ladm_u4_pu_asistencia_maestro_dacv.databinding.ActivityListBinding
import mx.edu.ittepic.ladm_u4_pu_asistencia_maestro_dacv.models.ListFile
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.net.URL


class ListActivity : AppCompatActivity() {

    lateinit var binding: ActivityListBinding
    lateinit var adapter : ListsViewAdapter
    lateinit var recyclerView: RecyclerView
    val arrayList = ArrayList<ListFile>()

    val VIEWER = 322
    val DOWNLOAD = 323

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.rvList

        obtenerListas()

        adapter = ListsViewAdapter(arrayList, object : ListsViewAdapter.onItemClickListenr{
            override fun onItemClick(list: ListFile, i: Int, tipo:Int) {
                var reporte = ""
                Log.i("######## 32",list.toString())
                FirebaseFirestore.getInstance()
                    .collection("listas")
                    .document(list.id)
                    .collection("lista")
                    .get()
                    .addOnSuccessListener {
                        for(doc in it){
                            reporte += doc.getString("id").toString()+", "+doc.getString("date").toString()+", "+doc.getString("hour").toString()+",\n"
                        }
                        if(tipo==VIEWER){
                            val builder = android.app.AlertDialog.Builder(this@ListActivity)
                            // Get the layout inflater
                            val inflater = this@ListActivity.layoutInflater;
                            val v = inflater.inflate(R.layout.dialog_list,null)
                            var tv_title = v.findViewById<TextView>(R.id.tv_title)
                            var tv_list = v.findViewById<TextView>(R.id.list_day)

                            tv_title.setText("${list.grupo} lista del día ${list.dia}\n")
                            tv_list.setText(reporte)
                            builder.setView(v)
                                .setPositiveButton("OK",
                                    DialogInterface.OnClickListener { dialog, id ->

                                    })
                            builder.create()
                            builder.show()
                        }
                        if(tipo ==DOWNLOAD){
                            if (ContextCompat.checkSelfPermission(
                                    this@ListActivity,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                )
                                != PackageManager.PERMISSION_GRANTED
                            ) {
                                ActivityCompat.requestPermissions(
                                    this@ListActivity,
                                    arrayOf(
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    ),
                                    32
                                )
                            }
                           // exportarCSV(list.id,reporte)
                            export(list.id,reporte)
                        }
                    }

            }

        })
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            32->{
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "#####hay permiso", Toast.LENGTH_SHORT).show()
                else{
                    Toast.makeText(this, "no hay permiso", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun exportarCSV(id:String, reporte: String) {
        val carpeta =
            File(Environment.getExternalStorageDirectory().toString() + "/listas")
        val archivoAgenda = "$carpeta/$id.csv"
        carpeta.createNewFile()
        carpeta.mkdirs()
        try {
            val fileWriter = FileWriter(archivoAgenda)

            fileWriter.append("gola,")
            val str = reporte.split(",")
            Log.i("######122",reporte)
            str.forEach {
                Log.i("##### str",it)
            }
           /* if () {
                fila.moveToFirst()
                do {
                    fileWriter.append(fila.getString(0))
                    fileWriter.append(",")
                    fileWriter.append(fila.getString(1))
                    fileWriter.append(",")
                    fileWriter.append(fila.getString(2))
                    fileWriter.append("\n")
                } while (fila.moveToNext())
            } else {
                Toast.makeText(this, "No hay registros.", Toast.LENGTH_LONG).show()
            }*/
            fileWriter.close()
            Toast.makeText(
                this,
                "SE CREO EL ARCHIVO CSV EXITOSAMENTE",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Log.i("##### 147",e.message.toString())
        }
    }

    fun export(id:String, reporte: String) {
        try {

            //saving file into device
            val out: FileOutputStream = openFileOutput("$id.csv", Context.MODE_PRIVATE)
            out.write(reporte.toString().toByteArray())
            out.close()

            //exporting
            val context: Context = applicationContext
            val filelocation = File(filesDir, "data.csv")
            val path: Uri = FileProvider.getUriForFile(
                context,
                "DACV",
                filelocation
            )
            val sendIntent = Intent(Intent.ACTION_SEND)
            sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(filelocation))
            sendIntent.type = "plain/text"
            startActivityForResult(Intent.createChooser(sendIntent,"Compartir"),33)
            startActivity(Intent.createChooser(sendIntent, "SHARE"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.i("######121 stringbuilder", reporte.toString())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 33 && resultCode == RESULT_OK) {
            if(data!=null)
                Toast.makeText(this, "Compartir", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this, "null", Toast.LENGTH_SHORT).show()
        }
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
                    list.id = doc.getString("id").toString()
                    arrayList.add(list)
                }
                adapter.notifyDataSetChanged()
            }
    }
}