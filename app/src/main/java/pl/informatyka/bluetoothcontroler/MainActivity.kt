package pl.informatyka.bluetoothcontroler

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.AsyncTask
import android.os.Vibrator
import android.util.Log
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.widget.*
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null
        lateinit var m_progress: ProgressDialog
        lateinit var m_bluetoothAdapter: BluetoothAdapter
        var m_isConnected: Boolean = false
        lateinit var m_address: String
    }

    lateinit var btn_disconnect: Button
    lateinit var btn_motor_group: com.google.android.material.button.MaterialButtonToggleGroup

    lateinit var btn_UP: Button
    lateinit var btn_DOWN: Button
    lateinit var btn_LEFT: Button
    lateinit var btn_RIGHT: Button
    lateinit var btn_STOP: Button

    lateinit var btn_motor_1: Button
    lateinit var btn_motor_2: Button
    lateinit var btn_motor_3: Button
    lateinit var btn_motor_4: Button

    lateinit var btn_light: androidx.appcompat.widget.AppCompatToggleButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        m_address = intent.getStringExtra(StartActivity.EXTRA_ADDRESS)!!


        ConnectToDevice(this).execute()

        btn_disconnect = findViewById(R.id.btnConnect)
        btn_motor_group = findViewById(R.id.btn_motor_group)
        btn_LEFT = findViewById(R.id.btn_left)
        btn_RIGHT = findViewById(R.id.btn_right)
        btn_STOP = findViewById(R.id.btn_stop)
        btn_light = findViewById(R.id.toggleLight)

        val imgOFF = resources.getDrawable(R.drawable.ic_doth_red)

        btn_disconnect.setOnClickListener {
            disconnect()
        }
        btn_motor_group.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked){
                when (checkedId){
                    R.id.btn_motor_1 -> command(1)
                    R.id.btn_motor_2 -> command(2)
                    R.id.btn_motor_3 -> command(3)
                    R.id.btn_motor_4 -> command(4)
                }
            }else if (!isChecked){
                when (checkedId){
                    R.id.btn_motor_1 -> btn_motor_1.setCompoundDrawablesWithIntrinsicBounds(null, null, null, imgOFF)
                    R.id.btn_motor_2 -> btn_motor_2.setCompoundDrawablesWithIntrinsicBounds(null, null, null, imgOFF)
                    R.id.btn_motor_3 -> btn_motor_3.setCompoundDrawablesWithIntrinsicBounds(null, null, null, imgOFF)
                    R.id.btn_motor_4 -> btn_motor_4.setCompoundDrawablesWithIntrinsicBounds(null, null, null, imgOFF)
                }
            }
        }

        holdToSend(btn_LEFT, "4")
        holdToSend(btn_RIGHT, "5")

        btn_STOP.setOnClickListener {
            sendCommand("S")
        }
        btn_light.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){
                sendCommand("O")
            }else{
                sendCommand("F")
            }
        }

    }

    private fun command(motor_NR: Int){
        val imgON = resources.getDrawable(R.drawable.ic_doth_green)

        btn_UP = findViewById(R.id.btn_up)
        btn_DOWN = findViewById(R.id.btn_down)
        btn_motor_1 = findViewById(R.id.btn_motor_1)
        btn_motor_2 = findViewById(R.id.btn_motor_2)
        btn_motor_3 = findViewById(R.id.btn_motor_3)
        btn_motor_4 = findViewById(R.id.btn_motor_4)

        if (motor_NR == 1){
            btn_motor_1.setCompoundDrawablesWithIntrinsicBounds(null, null, null, imgON)
            holdToSend(btn_UP, "6")
            holdToSend(btn_DOWN, "7")
        } else if (motor_NR == 2){
            btn_motor_2.setCompoundDrawablesWithIntrinsicBounds(null, null, null, imgON)
            holdToSend(btn_UP, "8")
            holdToSend(btn_DOWN, "9")
        } else if (motor_NR == 3){
            btn_motor_3.setCompoundDrawablesWithIntrinsicBounds(null, null, null, imgON)
            holdToSend(btn_UP, "A")
            holdToSend(btn_DOWN, "B")
        } else if (motor_NR == 4){
            btn_motor_4.setCompoundDrawablesWithIntrinsicBounds(null, null, null, imgON)
            holdToSend(btn_UP, "C")
            holdToSend(btn_DOWN, "D")
        }
    }

    private fun holdToSend(btn: Button, command: String){
        btn.setOnTouchListener(OnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                sendCommand(command)
            } else if (event.action == MotionEvent.ACTION_UP) {
                sendCommand("S")
            }
            false
        })
    }

    private fun sendCommand(input: String) {
        if (m_bluetoothSocket != null) {
            try{
                m_bluetoothSocket!!.outputStream.write(input.toByteArray())
            } catch(e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun disconnect() {
        if (m_bluetoothSocket != null) {
            try {
                m_bluetoothSocket!!.close()
                m_bluetoothSocket = null
                m_isConnected = false
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        finish()
    }

    private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>() {
        private var connectSuccess: Boolean = true
        private val context: Context

        init {
            this.context = c
        }

        override fun onPreExecute() {
            super.onPreExecute()
            m_progress = ProgressDialog.show(context, "Connecting...", "please wait")
        }

        override fun doInBackground(vararg p0: Void?): String? {
            try {
                if (m_bluetoothSocket == null || !m_isConnected) {
                    m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = m_bluetoothAdapter.getRemoteDevice(m_address)
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    m_bluetoothSocket!!.connect()
                }
            } catch (e: IOException) {
                connectSuccess = false
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (!connectSuccess) {
                Log.i("data", "couldn't connect")
            } else {
                m_isConnected = true
            }
            m_progress.dismiss()
        }
    }

}