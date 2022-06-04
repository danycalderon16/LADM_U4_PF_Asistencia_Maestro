package mx.edu.ittepic.ladm_u4_pu_asistencia_maestro_dacv

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.GregorianCalendar
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.ittepic.ladm_u4_pu_asistencia_maestro_dacv.adapters.ListAdapter
import mx.edu.ittepic.ladm_u4_pu_asistencia_maestro_dacv.databinding.ActivityMainBinding
import mx.edu.ittepic.ladm_u4_pu_asistencia_maestro_dacv.models.List
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    var bluetoothHeadset: BluetoothHeadset? = null

    // Get the default adapter
    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private val LOCATION_PERMISSION_REQUEST = 101
    private val BLUETOOTH_PERMISSION_REQUEST = 102
    private val SELECT_DEVICE = 102
    private val REQUEST_ENABLE_BT = 1
    private val MAC_ADDRESS = 301

    private lateinit var mySelectedBluetoothDevice: BluetoothDevice

    lateinit var m_address: String
    private var connectedDevice: String? = null
    private var adapterMainChat: ArrayAdapter<String>? = null
    val arrayMessage = ArrayList<String>()

    val MESSAGE_STATE_CHANGED = 0
    val MESSAGE_READ = 1
    val MESSAGE_WRITE = 2
    val MESSAGE_DEVICE_NAME = 3
    val MESSAGE_TOAST = 4


    val STATE_NONE = 0
    val STATE_LISTEN = 1
    val STATE_CONNECTING = 2
    val STATE_CONNECTED = 3

    val DEVICE_NAME = "deviceName"
    val TOAST = "toast"

    lateinit var chatUtils : MessageUtils

    lateinit var adapter :ListAdapter
    lateinit var recyclerView: RecyclerView
    val arrayList = ArrayList<List>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT), BLUETOOTH_PERMISSION_REQUEST
            )
        }

        binding.tvDevice.setOnClickListener {
            Log.i("######",arrayMessage.size.toString())
        }

        bluetoothAdapter?.getProfileProxy(this, profileListener, BluetoothProfile.HEADSET)
        binding.btnGuardar.setOnClickListener {
            escribir()
        }

        recyclerView = binding.recyclerView

        adapter = ListAdapter(arrayList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        adapterMainChat = ArrayAdapter(this, android.R.layout.simple_list_item_1)

       // binding.list.adapter = adapterMainChat

        chatUtils = MessageUtils(this,handler)

        binding.verListas.setOnClickListener {
            startActivity(Intent(this,ListActivity::class.java))
        }

    }

    private fun escribir() {
        var id = ""
        var cal = GregorianCalendar.getInstance()

        id = cal.get(Calendar.YEAR).toString()+
                cal.get(Calendar.MONTH).toString()+
                cal.get(Calendar.DAY_OF_MONTH).toString()+
                cal.get(Calendar.HOUR).toString()+
                cal.get(Calendar.MINUTE).toString()+
                cal.get(Calendar.SECOND).toString()+
                cal.get(Calendar.MILLISECOND).toString()


        val builder: AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Ingrese el nombre del grupo")

        val input = EditText(this)
        input.setHint("Grupo")
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("Guardar", DialogInterface.OnClickListener { dialog, which ->
            val data = hashMapOf(
                "id" to id,
                "grupo" to input.text.toString(),
                "dia" to cal.get(Calendar.DAY_OF_MONTH).toString()+"/"+
                        cal.get(Calendar.MONTH).toString()+"/"+
                        cal.get(Calendar.YEAR).toString()
            )

            if(arrayList.size==0){
                AlertDialog.Builder(this)
                    .setTitle("Atencion")
                    .setMessage("No hay asistencias")
            }

            FirebaseFirestore.getInstance()
                .collection("listas")
                .document(id)
                .set(data)
                .addOnSuccessListener {
                    arrayList.forEach {
                        val dataN = hashMapOf(
                            "id" to it.noControl,
                            "date" to it.date,
                            "hour" to it.hour,
                            "name" to it.name
                        )

                        FirebaseFirestore.getInstance()
                            .collection("listas")
                            .document(id)
                            .collection("lista")
                            .document(it.noControl)
                            .set(dataN)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Se creo la lista", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
        })
        builder.setNegativeButton("Cancelar", DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

        builder.show()


    }

    override fun onStart() {
        super.onStart()
        m_address = intent.getStringExtra("deviceAddress").toString()

        if (m_address == "") {
            Log.i("######## 94", "vacia")
        } else {
            Log.i("######## 96", m_address)
            bluetoothAdapter?.bondedDevices?.forEach { device ->
                val mac = device.address // MAC address
                if (mac == m_address) {
                    mySelectedBluetoothDevice = device
                    binding.tvDevice.setText(device.name.toString())
                    chatUtils.connect(device)
                }

            }
        }
    }

    private fun setState(subTitle: CharSequence) {
        binding.tvState.setText(subTitle)
    }


    private val profileListener = object : BluetoothProfile.ServiceListener {

        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.HEADSET) {
                bluetoothHeadset = proxy as BluetoothHeadset
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HEADSET) {
                bluetoothHeadset = null
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_search_devices -> {
                checkPermissions()
                true
            }
            R.id.menu_enable_bluetooth -> {
                enableBluetooth()
                true
            }
            R.id.menu_help -> {
                AlertDialog.Builder(this)
                    .setTitle("¡¡¡IMPORTANTE!!!")
                    .setMessage("Para poder conectarse con el alumno " +
                            "los dispositivos previamente YA DEBEN ESTAR VINCULADOS.\n" +
                            "Se recomienda primero dar conectar en el dispositivo ALUMNO, " +
                            "y después en el de MAESTRO.")
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) !== PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        } else {
            val intent = Intent(this, ListDeviceActivity::class.java)
            startActivityForResult(intent, SELECT_DEVICE)
        }
    }

    private fun enableBluetooth() {
        val BTadapter = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        BTadapter.getAdapter()
        if (BTadapter != null) {
            if (BTadapter.adapter?.isEnabled == false) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT),
                        BLUETOOTH_PERMISSION_REQUEST
                    )
                }
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
            if (BTadapter.adapter.isEnabled) {
                Toast.makeText(this, "El bluetooth ya está encendido", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "No se puede encender", Toast.LENGTH_LONG).show()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            BLUETOOTH_PERMISSION_REQUEST -> {
                Toast.makeText(this, "Se otorgo el permiso", Toast.LENGTH_SHORT).show()
            }
            LOCATION_PERMISSION_REQUEST -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("$$$$$", "Dentro")
                    val intent = Intent(this, ListDeviceActivity::class.java)
                    startActivityForResult(intent, SELECT_DEVICE)
                } else {
                    AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setMessage("Se requiere permiso de ubicación.\nPor favor concede")
                        .setPositiveButton("Conceder",
                            DialogInterface.OnClickListener { dialogInterface, i -> checkPermissions() })
                        .setNegativeButton("Denegar",
                            DialogInterface.OnClickListener { dialogInterface, i -> finish() })
                        .show()
                }
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST -> {
                val intent = Intent(this, ListDeviceActivity::class.java)
                startActivityForResult(intent, SELECT_DEVICE)
            }
            SELECT_DEVICE -> {
                if (requestCode == Activity.RESULT_OK) {
                    val address = data?.getStringExtra("deviceAddress")
                    Log.i("####### 205", address.toString())
                    // chatUtils.connect(bluetoothAdapter!!.getRemoteDevice(address))

                }
            }
            MAC_ADDRESS -> {
                val address = data?.getStringExtra("deviceAddress")
                Log.i("####### 212", address.toString())
            }
        }
    }


    private val handler = Handler { message ->
        Log.i("##### 266",message.what.toString())
        when (message.what) {
            MESSAGE_STATE_CHANGED -> when (message.arg1) {
                STATE_NONE -> setState("No Conectado")
                STATE_LISTEN -> setState("No Conectado")
                STATE_CONNECTING -> setState("Conectando...")
                STATE_CONNECTED -> setState("Conectado: $connectedDevice")
            }
            MESSAGE_WRITE -> {
                val buffer1 = message.obj as ByteArray
                val outputBuffer = String(buffer1)
                adapterMainChat?.add("Me: $outputBuffer")
                Log.i("####### 278", outputBuffer)
            }
            MESSAGE_READ -> {
                val buffer = message.obj as ByteArray
                val inputBuffer = String(buffer, 0, message.arg1)
                arrayMessage.add(inputBuffer)
                val current = LocalDateTime.now()

                var date = DateTimeFormatter.ofPattern("dd/mm/yyyy")
                var hour = DateTimeFormatter.ofPattern("HH:mm")
                var dateF = current.format(date)
                var hourF = current.format(hour)
                val list = List()
                list.noControl = inputBuffer
                list.date = dateF
                list.hour = hourF
                list.name = connectedDevice.toString()
                Log.i("###310",arrayList.size.toString())
                arrayList.add(list)
                Log.i("###312", arrayList[0].toString())
                adapterMainChat?.add(connectedDevice + ": " + inputBuffer)
                adapterMainChat?.notifyDataSetChanged()
                adapter.notifyDataSetChanged()
                //binding.list.deferNotifyDataSetChanged()
                Log.i("####### 284 read", inputBuffer)
            }
            MESSAGE_DEVICE_NAME -> {
                connectedDevice = message.data.getString(DEVICE_NAME)
                Toast.makeText(this, connectedDevice, Toast.LENGTH_SHORT).show()
            }
            MESSAGE_TOAST -> Toast.makeText(
                this,
                message.data.getString(TOAST),
                Toast.LENGTH_SHORT
            ).show()
        }
        false
    }
}