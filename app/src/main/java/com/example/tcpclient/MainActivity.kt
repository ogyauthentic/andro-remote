package com.example.tcpclient

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Vibrator
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.tcpclient.ui.theme.TcpClientTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.ConcurrentLinkedQueue

class MainActivity : ComponentActivity() {
    private val serverIp = "203.0.113.10"
    private val serverPort = 9999
    private val token = "mysecret123"
    private val commandQueue = ConcurrentLinkedQueue<String>()
    private var isProcessingQueue = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TcpClientTheme {
                RemoteControlScreen()
            }
        }
    }

    private fun enqueueCommand(command: String) {
        commandQueue.add(command)
        if (!isProcessingQueue) processQueue()
    }

    private fun processQueue() {
        if (isProcessingQueue) return
        isProcessingQueue = true
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                while (true) {
                    val command = commandQueue.poll() ?: break
                    executeCommand(command)
                }
            } finally {
                isProcessingQueue = false
                if (commandQueue.isNotEmpty()) processQueue()
            }
        }
    }

    private fun executeCommand(command: String) {
        try {
            val socket = Socket(serverIp, serverPort)
            val writer = PrintWriter(OutputStreamWriter(socket.getOutputStream()), true)
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

            writer.println("AUTH:$token")
            val auth = reader.readLine()
            if (auth == null || auth != "AUTH_OK") {
                runOnUiThread { Toast.makeText(this, "Autentikasi gagal", Toast.LENGTH_SHORT).show() }
                socket.close()
                return
            }

            writer.println("CMD:$command")
            val response = reader.readLine()

            when {
                response == null -> {
                    runOnUiThread { Toast.makeText(this, "Tidak ada respon", Toast.LENGTH_SHORT).show() }
                }
                response.startsWith("ACK:TOAST|") -> {
                    val msg = response.substringAfter("ACK:TOAST|")
                    runOnUiThread { Toast.makeText(this, msg, Toast.LENGTH_LONG).show() }
                }
                response.startsWith("ACK:URL|") -> {
                    val url = response.substringAfter("ACK:URL|")
                    runOnUiThread {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                    }
                }
                response == "ACK:VIBRATE" -> {
                    val vibrator = getSystemService(Vibrator::class.java)
                    vibrator?.vibrate(500)
                }
                response.startsWith("BATTERY:") -> {
                    val value = response.substringAfter("BATTERY:")
                    runOnUiThread { Toast.makeText(this, "Battery: $value%", Toast.LENGTH_SHORT).show() }
                }
                response.startsWith("GPS:") -> {
                    val gps = response.substringAfter("GPS:")
                    runOnUiThread { Toast.makeText(this, "GPS: $gps", Toast.LENGTH_SHORT).show() }
                }
                response.startsWith("FILES:") -> {
                    val files = response.substringAfter("FILES:")
                    runOnUiThread { Toast.makeText(this, "Files: $files", Toast.LENGTH_LONG).show() }
                }
                response.startsWith("DEVICE_INFO:") -> {
                    val info = response.substringAfter("DEVICE_INFO:")
                    runOnUiThread { Toast.makeText(this, info, Toast.LENGTH_LONG).show() }
                }
                response.startsWith("STATUS:") -> {
                    val info = response.substringAfter("STATUS:")
                    runOnUiThread { Toast.makeText(this, info, Toast.LENGTH_SHORT).show() }
                }
                response.startsWith("ERR:") -> {
                    runOnUiThread { Toast.makeText(this, response, Toast.LENGTH_LONG).show() }
                }
                else -> {
                    runOnUiThread { Toast.makeText(this, response, Toast.LENGTH_SHORT).show() }
                }
            }

            writer.close()
            reader.close()
            socket.close()
        } catch (e: SocketException) {
            runOnUiThread { Toast.makeText(this, "Socket error: ${e.message}", Toast.LENGTH_LONG).show() }
        } catch (e: Exception) {
            runOnUiThread { Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show() }
        }
    }

    @SuppressLint("MissingPermission")
    private fun readBatteryLevel(): Int {
        val batteryManager = getSystemService(BatteryManager::class.java)
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    @SuppressLint("MissingPermission")
    private fun readGpsLocation(): String {
        val locationManager = getSystemService(LocationManager::class.java)
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        for (provider in providers) {
            try {
                val location = locationManager.getLastKnownLocation(provider)
                if (location != null) {
                    return "lat=${location.latitude}|lng=${location.longitude}"
                }
            } catch (_: Exception) {
            }
        }
        return "lat=0.0|lng=0.0"
    }

    private fun listFiles(): String {
        val root = Environment.getExternalStorageDirectory()
        val files = root.listFiles()
        if (files == null || files.isEmpty()) {
            return "empty"
        }

        return files.take(10).joinToString(";") { it.name }
    }

    @Composable
    fun RemoteControlScreen() {
        var status by remember { mutableStateOf("Belum terhubung") }
        var log by remember { mutableStateOf("Log: siap") }
        val scope = rememberCoroutineScope()

        val sendCommand = { command: String ->
            scope.launch(Dispatchers.IO) {
                try {
                    val socket = Socket(serverIp, serverPort)
                    val writer = PrintWriter(OutputStreamWriter(socket.getOutputStream()), true)
                    val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                    writer.println("AUTH:$token")
                    val auth = reader.readLine()
                    if (auth == null || auth != "AUTH_OK") {
                        status = "Autentikasi gagal"
                        socket.close()
                        return@launch
                    }

                    writer.println("CMD:$command")
                    val response = reader.readLine()
                    if (response != null) {
                        when {
                            response.startsWith("EXEC:BATTERY") -> {
                                val battery = readBatteryLevel()
                                writer.println("RESULT:BATTERY:$battery")
                                status = "Battery: $battery%"
                            }
                            response.startsWith("EXEC:GPS") -> {
                                val gps = readGpsLocation()
                                writer.println("RESULT:GPS:$gps")
                                status = "GPS: $gps"
                            }
                            response.startsWith("EXEC:LIST_FILES") -> {
                                val files = listFiles()
                                writer.println("RESULT:FILES:$files")
                                status = "Files: $files"
                            }
                            response.startsWith("EXEC:TOAST|") -> {
                                val msg = response.substringAfter("EXEC:TOAST|")
                                runOnUiThread {
                                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show()
                                }
                                writer.println("RESULT:TOAST_OK")
                                status = "Toast sent"
                            }
                            response.startsWith("EXEC:URL|") -> {
                                val url = response.substringAfter("EXEC:URL|")
                                runOnUiThread {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    startActivity(intent)
                                }
                                writer.println("RESULT:URL_OK")
                                status = "Open URL: $url"
                            }
                            response == "EXEC:VIBRATE" -> {
                                val vibrator = getSystemService(Vibrator::class.java)
                                vibrator?.vibrate(500)
                                writer.println("RESULT:VIBRATE_OK")
                                status = "Vibrate OK"
                            }
                            response == "EXEC:REBOOT" -> {
                                writer.println("RESULT:REBOOT_OK")
                                status = "Reboot command accepted"
                            }
                            response == "EXEC:GET_STATUS" -> {
                                val battery = readBatteryLevel()
                                val gps = readGpsLocation()
                                writer.println("RESULT:STATUS:battery=$battery|gps=$gps")
                                status = "Status fetched"
                            }
                            else -> {
                                writer.println("RESULT:ACK:$response")
                                status = response
                            }
                        }
                    }
                    writer.close(); reader.close(); socket.close()
                } catch (e: Exception) {
                    status = "Error: ${e.message}"
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Remote Control Client", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = serverIp,
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Server IP") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = serverPort.toString(),
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Port") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = token,
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Token") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                val commands = listOf(
                    "PING",
                    "INFO",
                    "BATTERY",
                    "GPS",
                    "VIBRATE",
                    "TOAST|Halo dari server",
                    "URL|https://google.com",
                    "LIST_FILES",
                    "GET_STATUS",
                    "REBOOT"
                )

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(commands) { cmd ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { enqueueCommand(cmd) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(cmd)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(status)
                Text(log)
            }
        }
    }
}
